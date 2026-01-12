let allTokens = [];
let allAssets = [];
let allWallets = [];
let assetsByTokenId = {};
let websocketPrice = null;
let websocketAsset = null;
let priceHistory = {};
let tokenCharts = {};

document.addEventListener('DOMContentLoaded', () => {
    fetchHistoricTokenPrices();
    fetchAssets();
    fetchWallets();
    renderTokens();
    renderAssets();
    renderWallets();
    connectWebSocket();
});

async function fetchHistoricTokenPrices() {
    try {
        // first fetch all tokens
        const response = await fetch('/tokens');
        if (!response.ok) throw new Error('Failed to fetch tokens');

        allTokens = await response.json();

        // fetch historic prices for each token
        for (const token of allTokens) {
            try {
                const historyResponse = await fetch(`/historic/${token.id}?days=1`);
                if (historyResponse.ok) {
                    const prices = await historyResponse.json();
                    priceHistory[token.id] = {
                        prices: [],
                        timestamps: []
                    };
                    prices.forEach(p => {
                        priceHistory[token.id].prices.push(p.price);
                        priceHistory[token.id].timestamps.push(p.createdAt);
                    });
                } else {
                    priceHistory[token.id] = {
                        prices: [token.currentPrice],
                        timestamps: [new Date().toISOString()]
                    };
                }
            } catch (error) {
                console.error(`Failed to fetch history for ${token.id}:`, error);
                priceHistory[token.id] = {
                    prices: [token.currentPrice],
                    timestamps: [new Date().toISOString()]
                };
            }
        }

        renderTokens();
        console.log(priceHistory);
        document.getElementById('loadingContainer').style.display = 'none';
        document.getElementById('tokensContainer').style.display = 'grid';
    } catch (error) {
        console.error('Error fetching tokens:', error);
        document.getElementById('loadingContainer').style.display = 'none';
    }
}

async function fetchAssets() {
    try {
        const response = await fetch('/assets');
        if (!response.ok) throw new Error('Failed to fetch assets');

        allAssets = [];
        assetsByTokenId = {};

        allAssets = await response.json();

        allAssets.forEach(asset => {
            assetsByTokenId[asset.tokenId] = asset;
        });

        if (allAssets.length > 0) {
            const accountDataContainer = document.getElementById('accountDataContainer');
            if (accountDataContainer) {
                accountDataContainer.style.display = 'block';
            }
        } else {
            const accountDataContainer = document.getElementById('accountDataContainer');
            if (accountDataContainer) {
                accountDataContainer.style.display = 'none';
            }
        }

        renderAssets();

        document.getElementById('loadingContainer').style.display = 'none';
        document.getElementById('tokensContainer').style.display = 'grid';
    } catch (error) {
        console.error('Error fetching assets:', error);
        document.getElementById('loadingContainer').style.display = 'none';
    }
}

async function fetchWallets() {
    try {
        const response = await fetch('/wallets');
        if (!response.ok) throw new Error('Failed to fetch wallets');

        allWallets = await response.json();

        if (allWallets.length > 0) {
            const walletsDataContainer = document.getElementById('walletsDataContainer');
            if (walletsDataContainer) {
                walletsDataContainer.style.display = 'block';
            }
        } else {
            const walletsDataContainer = document.getElementById('walletsDataContainer');
            if (walletsDataContainer) {
                walletsDataContainer.style.display = 'none';
            }
        }

        renderWallets();

        document.getElementById('loadingContainer').style.display = 'none';
        document.getElementById('tokensContainer').style.display = 'grid';
    } catch (error) {
        console.error('Error fetching wallets:', error);
        document.getElementById('loadingContainer').style.display = 'none';
    }
}

function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsPriceUrl = `${protocol}//${window.location.host}/ws/prices`;
    const wsAssetUrl = `${protocol}//${window.location.host}/ws/assets`;

    websocketPrice = new WebSocket(wsPriceUrl);

    websocketPrice.onmessage = (event) => {
        try {
            const update = JSON.parse(event.data);
            updateTokenPrice(update.id, update.currentPrice, update.lastUpdated);
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

function renderTokens() {
    const container = document.getElementById('tokensContainer');
    container.innerHTML = '';

    document.getElementById('noResultsContainer').style.display = 'none';
    document.getElementById('tokensContainer').style.display = 'grid';

    allTokens.forEach(token => {
        const card = document.createElement('div');
        card.className = 'token-card';
        card.setAttribute('data-token-id', token.id);
        renderTokenCard(token, card);
        container.appendChild(card);
    });
}

function renderTokenCard(token, card) {
    const priceChangePercent = calculatePriceChange(token.id);
    const changeClass = priceChangePercent >= 0 ? 'positive' : 'negative';
    const changeSymbol = priceChangePercent >= 0 ? '▲' : '▼';

    const lastUpdated = formatTime(token.lastUpdated);

    card.innerHTML = `
                <div class="token-header">
                    <div class="token-info">
                        <h2>${token.name}</h2>
                        <h3>${token.ticker}</h3>
                    </div>
                </div>
                <div class="token-price">
                    <div class="price-label">Current Price</div>
                    <div class="current-price">$${token.currentPrice.toFixed(8)}</div>
                    <div class="price-change ${changeClass}">
                        <span>${changeSymbol} ${Math.abs(priceChangePercent).toFixed(8)}%</span>
                    </div>
                    <div class="last-updated">Updated ${lastUpdated}</div>
                </div>
                <div class="chart-container">
                    <canvas id="chart-${token.id}"></canvas>
                </div>
            `;

    setTimeout(() => drawTokenChart(token.id), 0);
}

function updateTokenPrice(id, price, timestamp) {
    const token = allTokens.find(t => t.id === id);
    if (!token) return;

    token.currentPrice = price;
    token.lastUpdated = timestamp;

    priceHistory[id].prices.push(token.currentPrice);
    priceHistory[id].timestamps.push(timestamp);

    // remove prices older than 24 hours (from the front of the queue)
    const now = new Date();
    const oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000);

    // pop from front while timestamps are older than 24 hours
    while (priceHistory[id].timestamps.length > 0) {
        const oldestTimestamp = new Date(priceHistory[id].timestamps[0]);
        if (oldestTimestamp < oneDayAgo) {
            priceHistory[id].prices.shift();
            priceHistory[id].timestamps.shift();
        } else {
            break;
        }
    }

    const cardElement = document.querySelector(`[data-token-id="${id}"]`);
    if (cardElement) {
        renderTokenCard(token, cardElement);
        drawTokenChart(id);
    }
}

function calculatePriceChange(tokenId) {
    const history = priceHistory[tokenId];
    if (!history || history.prices.length < 2) return 0;

    const newPrice = history.prices[history.prices.length - 1];
    const oldPrice = history.prices[0];

    return ((newPrice - oldPrice) / oldPrice) * 100;
}

function formatTime(timestamp) {
    if (!timestamp) return 'N/A';
    const date = new Date(timestamp);
    const now = new Date();
    const diff = Math.floor((now - date) / 1000);

    if (diff < 10) return 'just now';
    if (diff < 60) return `${Math.floor(diff)}s ago`;
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return date.toLocaleDateString();
}

function drawTokenChart(tokenId) {
    const history = priceHistory[tokenId];
    if (!history || history.prices.length === 0) return;

    const canvasElement = document.getElementById(`chart-${tokenId}`);
    if (!canvasElement) return;

    const ctx = canvasElement.getContext('2d');

    // replacing with new chart
    if (tokenCharts[tokenId]) {
        tokenCharts[tokenId].destroy();
    }

    // labels for time of the price
    const labels = history.timestamps.map((ts, i) => {
        const date = new Date(ts);
        return date.getHours() + ":" + (date.getMinutes() < 10 ? "0" : "") + date.getMinutes();
    });

    tokenCharts[tokenId] = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Price (USD)',
                data: history.prices,
                borderColor: '#a0a5c3',
                backgroundColor: 'rgb(177,177,226)',
                borderWidth: 2,
                fill: true,
                tension: 1,
                pointRadius: 0.5,
                pointBackgroundColor: '#b1bae0',
                pointBorderColor: '#ffffff',
                pointBorderWidth: 0.5,
                pointHoverRadius: 1,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false,
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0)',
                    padding: 10,
                    titleFont: { size: 15, weight: 'bold' },
                    bodyFont: { size: 10 },
                    borderColor: '#8593d9',
                    borderWidth: 1,
                    callbacks: {
                        label: function(context) {
                            return '$' + context.parsed.y.toLocaleString('en-US', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 8
                            });
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: false,
                    grid: { color: '#f0f0f0', drawBorder: false },
                    ticks: {
                        color: '#999999',
                        font: { size: 11 },
                        callback: function(value) {
                            return '$' + value.toLocaleString();
                        }
                    }
                },
                x: {
                    grid: { display: false, drawBorder: false },
                    ticks: {
                        color: '#999999',
                        font: { size: 10 }
                    }
                }
            }
        }
    });
}

function renderAssets() {
    const assetsList = document.getElementById('assetsList');

    if (!assetsList) return;

    if (allAssets.length === 0) {
        assetsList.innerHTML = '<div class="no-assets">No assets yet</div>';
        return;
    }

    assetsList.innerHTML = '';

    allAssets.forEach(asset => {
        const assetItem = document.createElement('div');
        assetItem.className = 'asset-item';
        assetItem.setAttribute('data-asset-id', asset.id);

        const totalValue = (asset.quantity * asset.price).toFixed(2);

        assetItem.innerHTML = `
            <div class="asset-ticker">${asset.ticker}</div>
            <div class="asset-name">${asset.name}</div>
            <div class="asset-quantity">Quantity: ${asset.quantity}</div>
            <div class="asset-total">Total: $${totalValue}</div>
        `;

        assetsList.appendChild(assetItem);
    });
}

function updateAsset(id, name, ticker, quantity, price) {
    const assetIndex = allAssets.findIndex(a => a.id === id);
    const update = { id, name, ticker, quantity, price };

    if (assetIndex !== -1) {
        allAssets[assetIndex] = { ...allAssets[assetIndex], ...update };
        assetsByTokenId[allAssets[assetIndex].tokenId] = allAssets[assetIndex];
    } else {
        allAssets.push(update);
        assetsByTokenId[update.tokenId] = update;
    }

    renderAssets();
}

function renderWallets() {
    const walletsList = document.getElementById('walletsList');

    if (!walletsList) return;

    if (allWallets.length === 0) {
        walletsList.innerHTML = '<div class="no-wallets">No wallets yet</div>';
        return;
    }

    walletsList.innerHTML = '';

    allWallets.forEach(wallet => {
        const walletItem = document.createElement('div');
        walletItem.className = 'wallet-item';
        walletItem.setAttribute('data-wallet-id', wallet.currency);

        walletItem.innerHTML = `
            <div class="wallet-currencty">${wallet.currency}</div>
            <div class="wallet-total">Total: ${wallet.total}</div>
        `;

        walletsList.appendChild(walletItem);
    });
}