let allTokens = [];
let websocket = null;
let priceHistory = {};

document.addEventListener('DOMContentLoaded', () => {
    fetchHistoricTokenPrices();
    renderTokens();
    console.log(priceHistory)
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

function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/prices`;

    websocket = new WebSocket(wsUrl);

    websocket.onmessage = (event) => {
        try {
            const update = JSON.parse(event.data);
            updateTokenPrice(update.id, update.currentPrice, update.lastUpdated);
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
        }
    };

    websocket.onerror = (error) => {
        console.error('WebSocket error:', error);
    };

    websocket.onclose = () => {
        // reconnect after 5 seconds
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
            `;
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
    if (diff < 60) return `${Math.floor(diff)}s ago`
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return date.toLocaleDateString();
}