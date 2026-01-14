let allTokens = [];
let allAssets = [];
let allWallets = [];
let assetsByTokenId = {};
let latestTransactions = [];

let websocketPrice = null;
let websocketAsset = null;
let websocketTransaction = null;

let priceHistory = {};
let tokenCharts = {};
let totalValue = null;
let currentMode = 0;
let modes = {
    0: "live",
    1: "training"
}

document.addEventListener('DOMContentLoaded', async () => {
    await fetchHistoricTokenPrices();
    await fetchAssets();
    await fetchWallets();
    await fetchTransactions();
    renderTokens();
    renderAssets();
    renderWallets();
    updateTotalValue();
    connectWebSocket();
});

function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsPriceUrl = `${protocol}//${window.location.host}/ws/prices`;
    const wsAssetUrl = `${protocol}//${window.location.host}/ws/assets`;
    const wsTransactionUrl = `${protocol}//${window.location.host}/ws/transactions`;

    websocketPrice = new WebSocket(wsPriceUrl);

    websocketPrice.onmessage = (event) => {
        try {
            const update = JSON.parse(event.data);
            updateTokenPrice(update.id, update.currentPrice, update.lastUpdated);
            updateTotalValue();
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
        }
    };
    websocketPrice.onerror = (error) => {
        console.error('WebSocket error:', error);
    };
    websocketPrice.onclose = () => {
        // try to reconnect after 5 seconds
        setTimeout(connectWebSocket, 5000);
    };

    websocketAsset = new WebSocket(wsAssetUrl);

    websocketAsset.onmessage = (event) => {
        try {
            const update = JSON.parse(event.data);
            updateAsset(update.id, update.name, update.ticker, update.quantity, update.price);
            updateTotalValue();
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
        }
    };
    websocketAsset.onerror = (error) => {
        console.error('WebSocket error:', error);
    };
    websocketAsset.onclose = () => {
        setTimeout(connectWebSocket, 5000);
    };

    websocketTransaction = new WebSocket(wsTransactionUrl);

    websocketTransaction.onmessage = (event) => {
        try {
            const update = JSON.parse(event.data);
            addTransaction(
                update.id,
                update.tokenName,
                update.tokenTicker,
                update.type,
                update.quantity,
                update.timestamp);
            removeOldestTransaction();
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
        }
    };

    websocketAsset.onerror = (error) => {
        console.error('WebSocket error:', error);
    };
    websocketAsset.onclose = () => {
        setTimeout(connectWebSocket, 5000);
    };
}