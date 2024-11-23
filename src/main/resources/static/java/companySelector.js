function loadCompanies() {
    fetch('/dashboard/companies')
        .then(response => response.json())
        .then(companies => {
            const select = document.getElementById('companySelect');
            select.innerHTML = '<option value="" disabled selected>Выберите компанию</option>';
            
            companies.forEach(company => {
                const option = document.createElement('option');
                option.value = company.id;
                option.textContent = company.name;
                select.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Ошибка при загрузке компаний:', error);
            alert('Не удалось загрузить список компаний');
        });
}

document.addEventListener('DOMContentLoaded', loadCompanies);

function sendRequest() {
    const companySelect = document.getElementById('companySelect');
    const selectedCompanyId = companySelect.value;

    if (!selectedCompanyId) {
        alert('Пожалуйста, выберите компанию');
        return;
    }

    fetch('/dashboard/companies/request', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            companyId: selectedCompanyId,
            message: "New user to company"
        })
    })
    .then(async response => {
        const text = await response.text();
        if (!response.ok) {
            throw new Error(text);
        }
        return text;
    })
    .then(message => {
        alert(message);
    })
    .catch(error => {
        console.error('Ошибка при отправке заявки:', error);
        alert(error.message);
    });
}