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
            const accountDataContainer = document.getElementById('assetDataContainer');
            if (accountDataContainer) {
                accountDataContainer.style.display = 'block';
            }
        } else {
            const accountDataContainer = document.getElementById('assetDataContainer');
            if (accountDataContainer) {
                accountDataContainer.style.display = 'none';
            }
        }

        renderAssets();

        document.getElementById('assetDataContainer').style.display = 'grid';
    } catch (error) {
        console.error('Error fetching assets:', error);
    }
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
