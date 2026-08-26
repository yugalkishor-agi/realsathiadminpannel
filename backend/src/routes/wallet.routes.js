const express = require("express");
const { supabase } = require("../config/supabase");
const { authenticateAccessToken } = require("../middlewares/auth.middleware");
const asyncHandler = require("../utils/async-handler");

const router = express.Router();

const DEFAULT_OPENING_COINS = 120;
const ALLOWED_KINDS = new Set(["audio_call", "video_call", "chat", "gift", "payment", "withdrawal", "payout", "adjustment"]);
const ALLOWED_STATUSES = new Set(["completed", "rejected", "pending", "processing", "failed"]);

function isSchemaError(error) {
    const code = String(error?.code || "").toUpperCase();
    const message = String(error?.message || "");
    return code === "PGRST204" ||
        code === "PGRST205" ||
        message.includes("schema cache") ||
        message.includes("Could not find") ||
        message.includes("column") ||
        message.includes("relation");
}

function normalizeChoice(value, allowed, fallback) {
    const normalized = String(value || "").trim().toLowerCase();
    return allowed.has(normalized) ? normalized : fallback;
}

function toInteger(value, fallback = 0) {
    const parsed = Number.parseInt(String(value ?? ""), 10);
    return Number.isFinite(parsed) ? parsed : fallback;
}

function fallbackOpeningTransaction() {
    return {
        id: "opening-balance",
        kind: "adjustment",
        title: "Welcome coins",
        detail: "Starting wallet balance",
        amountText: `+${DEFAULT_OPENING_COINS}`,
        coinsDelta: DEFAULT_OPENING_COINS,
        rupeesDelta: 0,
        rechargeAmountRupees: 0,
        status: "completed",
        counterpartyName: null,
        durationSeconds: null,
        messageCount: null,
        giftName: null,
        giftCount: 0,
        giftContextLabel: null,
        createdAt: new Date().toISOString()
    };
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
        giftName: metadata.giftName || null,
        giftCount: toInteger(metadata.giftCount),
        giftContextLabel: metadata.giftContextLabel || null,
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

    if (error && !isSchemaError(error)) {
        res.status(500).json({ message: "Unable to load wallet right now." });
        return;
    }

    const transactions = error ? [fallbackOpeningTransaction()] : (data || []).map(mapLedgerRow);
    const balanceCoins = transactions.reduce((total, item) => total + toInteger(item.coinsDelta), 0);

    res.status(200).json({
        balanceCoins: transactions.length > 0 ? Math.max(balanceCoins, 0) : DEFAULT_OPENING_COINS,
        transactions: transactions.length > 0 ? transactions : [fallbackOpeningTransaction()],
        source: error ? "fallback" : "ledger"
    });
}));

router.post("/record", authenticateAccessToken, asyncHandler(async (req, res) => {
    const kind = normalizeChoice(req.body?.kind, ALLOWED_KINDS, "adjustment");
    const status = normalizeChoice(req.body?.status, ALLOWED_STATUSES, "completed");
    const coinsDelta = toInteger(req.body?.coinsDelta ?? req.body?.coins_delta);
    const rupeesDelta = toInteger(req.body?.rupeesDelta ?? req.body?.rupees_delta);
    const rechargeAmountRupees = toInteger(req.body?.rechargeAmountRupees ?? req.body?.recharge_amount_rupees);
    const title = String(req.body?.title || "Wallet update").trim().slice(0, 120);
    const detail = String(req.body?.detail || "").trim().slice(0, 240);
    const amountText = String(req.body?.amountText || req.body?.amount_text || (coinsDelta >= 0 ? `+${coinsDelta}` : `${coinsDelta}`)).trim().slice(0, 40);
    const metadata = {
        counterpartyName: req.body?.counterpartyName || null,
        durationSeconds: req.body?.durationSeconds || null,
        messageCount: req.body?.messageCount || null,
        giftName: req.body?.giftName || null,
        giftCount: toInteger(req.body?.giftCount),
        giftContextLabel: req.body?.giftContextLabel || null
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

    if (error && !isSchemaError(error)) {
        res.status(500).json({ message: "Unable to record wallet transaction." });
        return;
    }

    res.status(200).json({
        recorded: !error,
        transaction: error ? null : mapLedgerRow(data)
    });
}));

module.exports = router;
