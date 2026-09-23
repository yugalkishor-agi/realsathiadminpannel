export const ADMIN_TABLES = [
  { key: 'users', label: 'Users', permission: 'users', columns: ['id', 'public_id', 'phone', 'username', 'role', 'account_status', 'host_status', 'created_at'] },
  { key: 'wallet_ledger', label: 'Wallet ledger', permission: 'wallet', columns: ['id', 'user_id', 'kind', 'title', 'coins_delta', 'rupees_delta', 'status', 'created_at'] },
  { key: 'recharge_orders', label: 'Recharge orders', permission: 'wallet', columns: ['order_id', 'user_id', 'coins', 'amount_rupees', 'status', 'created_at'] },
  { key: 'user_reports', label: 'Reports', permission: 'moderation', columns: ['id', 'reporter_id', 'reported_id', 'reason', 'status', 'created_at'] },
  { key: 'host_kyc_details', label: 'Host KYC', permission: 'kyc', columns: ['user_id', 'full_name', 'status', 'created_at', 'updated_at'] },
  { key: 'user_blocks', label: 'Blocks', permission: 'moderation', columns: ['id', 'blocker_id', 'blocked_id', 'created_at'] },
  { key: 'recharge_receipts', label: 'Receipts', permission: 'wallet', columns: ['id', 'order_id', 'user_id', 'status', 'amount_rupees', 'created_at'] },
]
