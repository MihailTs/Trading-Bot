let allTokens = [];
let allAssets = [];
let allWallets = [];
let assetsByTokenId = {};
let websocketPrice = null;
let websocketAsset = null;
let priceHistory = {};
let tokenCharts = {};
let totalValue = null;

document.addEventListener('DOMContentLoaded', async () => {
    await fetchHistoricTokenPrices();
    await fetchAssets();
    await fetchWallets();
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
}