export function createSearchableSelect(originalSelect, items, {
    getDisplayText = (item) => `${item.firstName} ${item.lastName}`,
    getValue = (item) => item.userId,
    placeholder = 'Выберите значение',
    searchPlaceholder = 'Поиск...',
    onSelect = null
} = {}) {
    const selectWrapper = document.createElement('div');
    selectWrapper.className = 'custom-select-wrapper';
    originalSelect.parentNode.insertBefore(selectWrapper, originalSelect);
    
    const selectButton = document.createElement('div');
    selectButton.className = 'select-button';
    selectButton.innerHTML = `
        <span class="selected-value">${placeholder}</span>
        <span class="arrow">▼</span>
    `;
    
    const dropdownContainer = document.createElement('div');
    dropdownContainer.className = 'dropdown-container';
    
    const searchInput = document.createElement('input');
    searchInput.type = 'text';
    searchInput.className = 'select-search';
    searchInput.placeholder = searchPlaceholder;
    
    const optionsContainer = document.createElement('div');
    optionsContainer.className = 'select-options';
    
    dropdownContainer.appendChild(searchInput);
    dropdownContainer.appendChild(optionsContainer);
    selectWrapper.appendChild(selectButton);
    selectWrapper.appendChild(dropdownContainer);
    
    originalSelect.removeAttribute('required');
    originalSelect.style.display = 'none';
    
    const validateAndSetValue = (item) => {
        const option = document.createElement('option');
        option.value = getValue(item);
        option.textContent = getDisplayText(item);
        originalSelect.innerHTML = '';
        originalSelect.appendChild(option);
        
        originalSelect.value = getValue(item);
        selectButton.querySelector('.selected-value').textContent = getDisplayText(item);
        dropdownContainer.classList.remove('show');
        selectButton.classList.remove('active');
        
        selectWrapper.classList.add('valid');
        selectWrapper.classList.remove('invalid');
        
        const event = new Event('change', { bubbles: true });
        originalSelect.dispatchEvent(event);
        
        console.log('Selected value after set:', originalSelect.value);
        
        if (onSelect) {
            onSelect(item);
        }
    };
    
    const updateOptionsContainer = (items) => {
        optionsContainer.innerHTML = '';
        if (items.length === 0) {
            const noResults = document.createElement('div');
            noResults.className = 'select-option no-results';
            noResults.textContent = 'Нет результатов';
            optionsContainer.appendChild(noResults);
            return;
        }
        
        items.forEach(item => {
            const option = document.createElement('div');
            option.className = 'select-option';
            option.textContent = getDisplayText(item);
            option.dataset.value = getValue(item);
            
            option.addEventListener('click', () => {
                validateAndSetValue(item);
                if (onSelect) onSelect(item);
            });
            
            optionsContainer.appendChild(option);
        });
    };
    
    selectButton.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdownContainer.classList.toggle('show');
        selectButton.classList.toggle('active');
        if (dropdownContainer.classList.contains('show')) {
            updateOptionsContainer(items);
        }
    });

    searchInput.addEventListener('input', () => {
        const searchTerm = searchInput.value.toLowerCase();
        const filteredItems = items.filter(item => 
            getDisplayText(item).toLowerCase().includes(searchTerm)
        );
        updateOptionsContainer(filteredItems);
    });

    document.addEventListener('click', () => {
        dropdownContainer.classList.remove('show');
        selectButton.classList.remove('active');
    });

    dropdownContainer.addEventListener('click', (e) => {
        e.stopPropagation();
    });

    searchInput.addEventListener('click', (e) => {
        e.stopPropagation();
    });

    const form = originalSelect.closest('form');
    if (form) {
        form.addEventListener('submit', (e) => {
            console.log('Form submitted, current select value:', originalSelect.value);
            console.log('Current select options:', originalSelect.innerHTML);
            
            if (!originalSelect.value) {
                e.preventDefault();
                selectWrapper.classList.add('invalid');
                selectWrapper.classList.remove('valid');
                alert('Пожалуйста, выберите руководителя подотдела');
            }
        });
    }

    return {
        update: (newItems) => {
            items = newItems;
            updateOptionsContainer(items);
        },
        reset: () => {
            originalSelect.value = '';
            selectButton.querySelector('.selected-value').textContent = placeholder;
            selectWrapper.classList.remove('valid');
            selectWrapper.classList.add('invalid');
            updateOptionsContainer(items);
        },
        setValue: (value, displayText) => {
            originalSelect.value = value;
            selectButton.querySelector('.selected-value').textContent = displayText;
            selectWrapper.classList.add('valid');
            selectWrapper.classList.remove('invalid');
            
            const event = new Event('change', { bubbles: true });
            originalSelect.dispatchEvent(event);
        }
    };
}