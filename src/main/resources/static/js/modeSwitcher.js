document.addEventListener('DOMContentLoaded', () => {
    initializeModeSwitcher();
    createTrainingControlsUI();
});

function initializeModeSwitcher() {
    const modeButtons = document.querySelectorAll('.mode-btn');
    modeButtons.forEach(btn => {
        btn.addEventListener('click', handleModeClick);
    });
}

function handleModeClick(event) {
    const mode = event.target.dataset.mode;

    if (mode === 'live') {
        switchToLiveMode();
    } else if (mode === 'training') {
        showTrainingControls();
    }
}

async function switchToLiveMode() {
    const btn = document.querySelector('[data-mode="live"]');

    try {
        const response = await fetch('/mode/set?mode=LIVE', {
            method: 'POST'
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to set live mode');
        }

        currentMode = 0;

        document.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        hideTrainingControls();

        clearAllData();

        await refetchAllData();
    } catch (error) {
        console.error('Error switching to live mode:', error);
        alert('Error: ' + error.message);
    }
}

async function refetchAllData() {
    try {
        await fetchHistoricTokenPrices();
        await fetchAssets();
        await fetchWallets();
        await fetchTransactions();
        renderTokens();
        renderAssets();
        renderWallets();
        updateTotalValue();
    } catch (error) {
        console.error('Error refetching data:', error);
        alert('Error refetching data: ' + error.message);
    }
}

function clearAllData() {
    allTokens = [];
    allAssets = [];
    allWallets = [];
    assetsByTokenId = {};
    latestTransactions = [];
    priceHistory = {};
    tokenCharts = {};

    totalValue = null;

    if (websocketPrice && websocketPrice.readyState === WebSocket.OPEN) {
        websocketPrice.close();
        websocketPrice = null;
    }
    if (websocketAsset && websocketAsset.readyState === WebSocket.OPEN) {
        websocketAsset.close();
        websocketAsset = null;
    }
    if (websocketTransaction && websocketTransaction.readyState === WebSocket.OPEN) {
        websocketTransaction.close();
        websocketTransaction = null;
    }

    Object.keys(tokenCharts).forEach(tokenId => {
        if (tokenCharts[tokenId]) {
            tokenCharts[tokenId].destroy();
        }
    });
    tokenCharts = {};

    const tokensContainer = document.getElementById('tokensContainer');
    const assetsContainer = document.getElementById('assetsContainer');
    const walletsContainer = document.getElementById('walletsContainer');
    const transactionsContainer = document.getElementById('transactionsContainer');
    const totalValueDisplay = document.getElementById('totalValue');
    const assetsList = document.getElementById('assetsList');
    const assetDataContainer = document.getElementById('assetDataContainer');

    if (tokensContainer) tokensContainer.innerHTML = '';
    if (assetsContainer) assetsContainer.innerHTML = '';
    if (walletsContainer) walletsContainer.innerHTML = '';
    if (transactionsContainer) transactionsContainer.innerHTML = '';
    if (totalValueDisplay) totalValueDisplay.textContent = '$0.00';
    if (assetsList) assetsList.innerHTML = '';
    if (assetDataContainer) assetDataContainer.style.display = 'none';
}


function createTrainingControlsUI() {
    const controlsHTML = `
        <div id="trainingControls" class="training-controls" style="display: none;">
            <div class="training-inputs">
                <div class="date-input-group">
                    <label for="trainingStartDate">Start Date</label>
                    <input type="date" id="trainingStartDate">
                </div>
                <div class="date-input-group">
                    <label for="trainingEndDate">End Date</label>
                    <input type="date" id="trainingEndDate">
                </div>
                <div id="trainingError" class="training-error" style="display: none;"></div>
                <div class="training-buttons">
                    <button class="btn btn-secondary" id="cancelTrainingBtn">Cancel</button>
                    <button class="btn btn-primary" id="confirmTrainingBtn">Start Training</button>
                </div>
            </div>
        </div>
    `;

    const modeSwitcherContainer = document.querySelector('.mode-switcher-container');
    modeSwitcherContainer.insertAdjacentHTML('afterend', controlsHTML);

    setDefaultTrainingDates();
    document.getElementById('confirmTrainingBtn').addEventListener('click', confirmTrainingMode);
    document.getElementById('cancelTrainingBtn').addEventListener('click', hideTrainingControls);
}

function setDefaultTrainingDates() {
    const today = new Date();
    const pastDate = new Date();
    pastDate.setDate(pastDate.getDate() - 30);

    const formatDate = (date) => date.toISOString().split('T')[0];

    document.getElementById('trainingStartDate').value = formatDate(pastDate);
    document.getElementById('trainingEndDate').value = formatDate(today);
}

function showTrainingControls() {
    const trainingControls = document.getElementById('trainingControls');
    trainingControls.style.display = 'flex';

    // Update button state
    document.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('active'));
    document.querySelector('[data-mode="training"]').classList.add('active');
}

function hideTrainingControls() {
    const trainingControls = document.getElementById('trainingControls');
    trainingControls.style.display = 'none';
    document.getElementById('trainingError').style.display = 'none';
}

async function confirmTrainingMode() {
    const btn = document.getElementById('confirmTrainingBtn');
    const startDateInput = document.getElementById('trainingStartDate').value;
    const endDateInput = document.getElementById('trainingEndDate').value;
    const errorDiv = document.getElementById('trainingError');

    errorDiv.style.display = 'none';

    // Validation
    if (!startDateInput || !endDateInput) {
        showTrainingError('Please select both start and end dates');
        return;
    }

    const start = new Date(startDateInput);
    const end = new Date(endDateInput);

    if (start >= end) {
        showTrainingError('Start date must be before end date');
        return;
    }

    const daysDiff = (end - start) / (1000 * 60 * 60 * 24);
    if (daysDiff > 365) {
        showTrainingError('Training data range cannot exceed 365 days');
        return;
    }

    try {
        const startDateTime = formatDateToISO(startDateInput, true);
        const endDateTime = formatDateToISO(endDateInput, false);

        const params = new URLSearchParams({
            startDate: startDateTime,
            endDate: endDateTime
        });

        const response = await fetch(`/mode/training?${params}`, {
            method: 'POST'
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }

        currentMode = 1;

        document.querySelectorAll('.mode-btn').forEach(b => b.classList.remove('active'));
        document.querySelector('[data-mode="training"]').classList.add('active');
        hideTrainingControls();

        clearAllData();

        await refetchAllData();
    } catch (error) {
        console.error('Error switching to training mode:', error);
        showTrainingError('Error: ' + error.message);
    }
}

function formatDateToISO(dateString, isStart) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const time = isStart ? '00:00:00' : '23:59:59';
    return `${year}-${month}-${day}T${time}`;
}

function showTrainingError(message) {
    const errorDiv = document.getElementById('trainingError');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
}