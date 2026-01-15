async function fetchTransactions() {
    try {
        // fetching only the first page of transactions
        // TODO: add pagination
        const response = await fetch(`/transactions/${modes[currentMode]}?page=0&pageSize=50`);
        if (!response.ok) throw new Error('Failed to fetch transactions');

        latestTransactions = await response.json();

        if (latestTransactions.length > 0) {
            const transactionsDataContainer = document.getElementById('transactionsDataContainer');
            if (transactionsDataContainer) {
                transactionsDataContainer.style.display = 'block';
            }
        } else {
            const transactionsDataContainer = document.getElementById('transactionsDataContainer');
            if (transactionsDataContainer) {
                transactionsDataContainer.style.display = 'none';
            }
        }

        renderTransactions();

        document.getElementById('transactionsDataContainer').style.display = 'grid';
    } catch (error) {
        console.error('Error fetching transactions:', error);
    }
}

function renderTransactions() {
    const transactionsList = document.getElementById('transactionsList');

    if (!transactionsList) return;

    if (latestTransactions.length === 0) {
        transactionsList.innerHTML = '<div class="no-assets">No assets yet</div>';
        return;
    }

    const fragment = document.createDocumentFragment();

    latestTransactions.forEach(transaction => {
        const transactionItem = document.createElement('div');
        transactionItem.className = 'transaction-item';
        transactionItem.setAttribute('data-transaction-id', transaction.id);

        transactionItem.innerHTML = `
            <div class="transaction-token-ticker">${transaction.tokenTicker}</div>
            <div class="transaction-token-name">${transaction.tokenName}</div>
            <div class="transaction-type">Type: ${transaction.type}</div>
            <div class="transaction-quantity">Quantity: ${transaction.quantity}</div>
            <div class="transaction-timestamp">
                Time: ${
                    new Date(transaction.timestamp).toISOString().slice(0, 16).replace('T', '  ')
                }
            </div>
        `;

        fragment.appendChild(transactionItem);
    });

    transactionsList.innerHTML = '';
    transactionsList.appendChild(fragment);
}

function addTransaction(id, tokenName, tokenTicker, type, quantity, timestamp) {
    latestTransactions.unshift({
        id: id,
        tokenName: tokenName,
        tokenTicker: tokenTicker,
        type: type,
        quantity: quantity,
        timestamp: timestamp
    });

    renderTransactions();
}

function removeOldestTransaction() {
    if (latestTransactions.length > 50) {
        latestTransactions.pop();
    }
}