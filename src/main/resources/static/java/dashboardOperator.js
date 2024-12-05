
document.addEventListener('DOMContentLoaded', () => {
    const tabs = document.querySelectorAll('.tab');
    const contents = document.querySelectorAll('.content');

    let activeTab = tabs[0];
    let activeContent = contents[0];
    let isAnimating = false;

    activeTab.classList.add('active');
    activeContent.style.top = '0';
    activeContent.style.opacity = '1';
    activeContent.style.zIndex = '2';

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            if (tab === activeTab || isAnimating) return;

            isAnimating = true;

            activeTab.classList.remove('active');
            tab.classList.add('active');
            activeTab = tab;

            const targetContent = document.querySelector(`#${tab.dataset.target}`);

            activeContent.style.transition = 'top 0.5s ease-in-out, opacity 0.5s ease-in-out';
            activeContent.style.top = '-100%';
            activeContent.style.opacity = '0';

            targetContent.style.transition = 'none';
            targetContent.style.top = '100%';
            targetContent.style.opacity = '0';
            targetContent.style.zIndex = '2';

            setTimeout(() => {
                targetContent.style.transition = 'top 0.5s ease-in-out, opacity 0.5s ease-in-out';
                targetContent.style.top = '0';
                targetContent.style.opacity = '1';

                setTimeout(() => {
                    activeContent.style.zIndex = '1';
                    targetContent.style.zIndex = '2';
                    activeContent = targetContent;
                    isAnimating = false;
                }, 500);
            }, 50);
        });
    });
});


document.addEventListener('DOMContentLoaded', function() {
    loadNotifications();
});

function loadNotifications() {
    fetch('/dashboard/received')
        .then(response => response.json())
        .then(notifications => {
            const container = document.getElementById('notifications-container');
            container.innerHTML = '';

            if (notifications.length === 0) {
                container.innerHTML = '<p class="notification-not-message">Нет новых уведомлений</p>';
                return;
            }

            notifications.forEach(notification => {
                const notificationElement = createNotificationElement(notification);
                container.appendChild(notificationElement);
            });
        })
        .catch(error => {
            console.error('Ошибка при загрузке уведомлений:', error);
        });
}

function createNotificationElement(notification) {
    const div = document.createElement('div');
    div.className = 'notification-item';
    if (notification.type == "simpleMessage"){
        console.log(notification.type);
        div.innerHTML = `
        <div class="notification-content">
            <p class="notification-message">${notification.message}</p>
            <p class="notification-details">
                От: ${notification.senderName}<br>
                Компания: ${notification.companyName}<br>
                Дата: ${formatDate(notification.sendDate)}
            </p>
            <div class="notification-actions">
                <button class="readNotification" onclick="readRequest(${notification.id})">Отметить прочитанным</button>
            </div>
        </div>
        `;
    }
    else if (notification.type == "actionMessage"){
        div.innerHTML = `
        <div class="notification-content">
            <p class="notification-message">${notification.message}</p>
            <p class="notification-details">
                От: ${notification.senderName}<br>
                Компания: ${notification.companyName}<br>
                Дата: ${formatDate(notification.sendDate)}
            </p>
            <div class="notification-actions">
                <button class="accept-button" onclick="acceptRequest(${notification.id})">Принять</button>
                <button class="reject-button" onclick="rejectRequest(${notification.id})">Отклонить</button>
            </div>
        </div>
        `;
    }

    
    return div;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU');
}

function acceptRequest(notificationId) {
    fetch(`/dashboard/notifications/accept/${notificationId}`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw new Error(err.message); });
        }
        return response.json();
    })
    .then(data => {
        alert(data.message);
        loadNotifications();
    })
    .catch(error => {
        console.error('Ошибка при принятии заявки:', error);
        alert('Ошибка при принятии заявки: ' + error.message);
    });
}

function rejectRequest(notificationId) {
    fetch(`/dashboard/notifications/reject/${notificationId}`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw new Error(err.message); });
        }
        return response.json();
    })
    .then(data => {
        alert(data.message);
        loadNotifications();
    })
    .catch(error => {
        console.error('Ошибка при принятии заявки:', error);
        alert('Ошибка при принятии заявки: ' + error.message);
    });
}

function readRequest(notificationId) {
    fetch(`/dashboard/notifications/read/${notificationId}`, {
        method: 'POST',
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw new Error(err.message); });
        }
        return response.json();
    })
    .then(data => {
        alert(data.message);
        loadNotifications();
    })
    .catch(error => {
        console.error('Ошибка при принятии заявки:', error);
        alert('Ошибка при принятии заявки: ' + error.message);
    });
}
