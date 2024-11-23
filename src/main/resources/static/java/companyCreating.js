document.getElementById('createCompanyForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const companyData = {
        companyName: document.getElementById('companyName').value,
        address: document.getElementById('address').value
    };

    // const companyData = new FormData(form);

    fetch('/dashboard/starter/createCompany/newcompany', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(companyData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Ошибка входа');
        }
        return response.json();
    })
    .then(data => {
        console.log('Успех:', data);
        window.location.href = '/dashboard';
    })
});