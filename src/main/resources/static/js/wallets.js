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

function updateTotalValue() {
    totalValue = 0;
    console.log(allAssets);
    console.log(allWallets);
    allWallets.forEach(wallet => totalValue += wallet.total);
    allAssets.forEach(asset => totalValue += asset.quantity * asset.price);
    totalValue = totalValue.toFixed(2)
    renderTotalValue();
}

function renderTotalValue() {
    const valueLabel = document.getElementById('totalValue');

    if (!valueLabel) return;

    document.getElementById('totalValueContainer').style.display = 'block';
    if (totalValue == null) {
        valueLabel.innerHTML = 'Value cannot be displayed';
        return;
    }

    valueLabel.innerHTML = `${totalValue}`;
}