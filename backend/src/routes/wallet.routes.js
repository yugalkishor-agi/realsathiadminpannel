const express = require("express");
const { supabase } = require("../config/supabase");
const { authenticateAccessToken } = require("../middlewares/auth.middleware");
const asyncHandler = require("../utils/async-handler");

const router = express.Router();

const ALLOWED_KINDS = new Set(["audio_call", "video_call", "chat", "payment", "withdrawal", "payout", "adjustment"]);
const ALLOWED_STATUSES = new Set(["completed", "rejected", "pending", "processing", "failed"]);

function normalizeChoice(value, allowed, fallback) {
    const normalized = String(value || "").trim().toLowerCase();
    return allowed.has(normalized) ? normalized : fallback;
}

function toInteger(value, fallback = 0) {
    const parsed = Number.parseInt(String(value ?? ""), 10);
    return Number.isFinite(parsed) ? parsed : fallback;
}

function mapLedgerRow(row) {
    const metadata = row.metadata && typeof row.metadata === "object" ? row.metadata : {};
    return {
        id: String(row.id || ""),
        kind: String(row.kind || "adjustment"),
        title: String(row.title || "Wallet update"),
        detail: String(row.detail || ""),
        amountText: String(row.amount_text || row.amountText || ""),
        coinsDelta: toInteger(row.coins_delta ?? row.coinsDelta),
        rupeesDelta: toInteger(row.rupees_delta ?? row.rupeesDelta),
        rechargeAmountRupees: toInteger(row.recharge_amount_rupees ?? row.rechargeAmountRupees),
        status: String(row.status || "completed"),
        counterpartyName: metadata.counterpartyName || null,
        durationSeconds: metadata.durationSeconds || null,
        messageCount: metadata.messageCount || null,
        createdAt: row.created_at || row.createdAt || new Date().toISOString()
    };
}

router.get("/summary", authenticateAccessToken, asyncHandler(async (req, res) => {
    const { data, error } = await supabase
        .from("wallet_ledger")
        .select("*")
        .eq("user_id", req.auth.userId)
        .order("created_at", { ascending: false })
        .limit(100);

    if (error) {
        res.status(500).json({ message: "Unable to load wallet right now." });
        return;
    }

    const transactions = (data || []).map(mapLedgerRow);
    const { data: receipts, error: receiptsError } = await supabase.from("recharge_receipts")
        .select("*").eq("user_id", req.auth.userId).order("created_at", { ascending: false }).limit(100);
    if (receiptsError) throw receiptsError;
    const balanceCoins = transactions.reduce((total, item) =>
        total + (item.status === "completed" ? toInteger(item.coinsDelta) : 0), 0);

    res.status(200).json({
        balanceCoins: Math.max(balanceCoins, 0),
        transactions,
        rechargeUpdates: (receipts || []).map((row) => ({
            id: row.id, orderId: row.order_id, status: row.status,
            coins: row.coins, amountRupees: row.amount_rupees,
            createdAt: row.created_at, orderCreatedAt: row.order_created_at
        })),
        source: "ledger"
    });
}));

router.post("/record", authenticateAccessToken, asyncHandler(async (req, res) => {
    const kind = normalizeChoice(req.body?.kind, ALLOWED_KINDS, "adjustment");
    const status = normalizeChoice(req.body?.status, ALLOWED_STATUSES, "completed");
    const coinsDelta = toInteger(req.body?.coinsDelta ?? req.body?.coins_delta);
    const rupeesDelta = toInteger(req.body?.rupeesDelta ?? req.body?.rupees_delta);
    const rechargeAmountRupees = toInteger(req.body?.rechargeAmountRupees ?? req.body?.recharge_amount_rupees);
    if (!["audio_call", "video_call", "chat"].includes(kind) ||
        status !== "completed" || coinsDelta >= 0 || coinsDelta < -10000 ||
        rupeesDelta !== 0 || rechargeAmountRupees !== 0) {
        res.status(422).json({ message: "This transaction cannot be recorded from the app." });
        return;
    }
    const title = String(req.body?.title || "Wallet update").trim().slice(0, 120);
    const detail = String(req.body?.detail || "").trim().slice(0, 240);
    const amountText = String(req.body?.amountText || req.body?.amount_text || (coinsDelta >= 0 ? `+${coinsDelta}` : `${coinsDelta}`)).trim().slice(0, 40);
    const metadata = {
        counterpartyName: req.body?.counterpartyName || null,
        durationSeconds: req.body?.durationSeconds || null,
        messageCount: req.body?.messageCount || null
    };

    const { data, error } = await supabase
        .from("wallet_ledger")
        .insert({
            user_id: req.auth.userId,
            kind,
            title,
            detail,
            amount_text: amountText,
            coins_delta: coinsDelta,
            rupees_delta: rupeesDelta,
            recharge_amount_rupees: rechargeAmountRupees,
            status,
            metadata
        })
        .select("*")
        .single();

    if (error) {
        res.status(500).json({ message: "Unable to record wallet transaction." });
        return;
    }

    res.status(200).json({
        recorded: true,
        transaction: mapLedgerRow(data)
    });
}));

module.exports = router;
