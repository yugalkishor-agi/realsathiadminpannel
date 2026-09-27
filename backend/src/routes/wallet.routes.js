const express = require("express");
const { supabase } = require("../config/supabase");
const { authenticateAccessToken } = require("../middlewares/auth.middleware");
const asyncHandler = require("../utils/async-handler");

const router = express.Router();

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

module.exports = router;
