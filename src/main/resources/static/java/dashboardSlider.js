
document.addEventListener('DOMContentLoaded', () => {
    const tabs = document.querySelectorAll('.tab');
    const contents = document.querySelectorAll('.content');

    let activeTab = tabs[0];
    let activeContent = contents[0];
    let isAnimating = false;

    // Инициализация начального состояния
    activeTab.classList.add('active');
    activeContent.style.top = '0';
    activeContent.style.opacity = '1';
    activeContent.style.zIndex = '2';

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            if (tab === activeTab || isAnimating) return;

            // Блокируем переключение
            isAnimating = true;

            // Обновление табов
            activeTab.classList.remove('active');
            tab.classList.add('active');
            activeTab = tab;

            // Найти цель
            const targetContent = document.querySelector(`#${tab.dataset.target}`);

            // Анимация выхода текущего контента
            activeContent.style.transition = 'top 0.5s ease-in-out, opacity 0.5s ease-in-out';
            activeContent.style.top = '-100%';
            activeContent.style.opacity = '0';

            // Подготовка нового контента
            targetContent.style.transition = 'none';
            targetContent.style.top = '100%';
            targetContent.style.opacity = '0';
            targetContent.style.zIndex = '2';

            // Запуск анимации нового контента
            setTimeout(() => {
                targetContent.style.transition = 'top 0.5s ease-in-out, opacity 0.5s ease-in-out';
                targetContent.style.top = '0';
                targetContent.style.opacity = '1';

                // Сброс состояния через время анимации
                setTimeout(() => {
                    activeContent.style.zIndex = '1';
                    targetContent.style.zIndex = '2';
                    activeContent = targetContent;
                    isAnimating = false;
                }, 500); // Время завершения анимации
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
            container.innerHTML = ''; // Очищаем контейнер

            if (notifications.length === 0) {
                container.innerHTML = '<p>Нет новых уведомлений</p>';
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
    
    div.innerHTML = `
        <div class="notification-content">
            <p class="notification-message">${notification.message}</p>
            <p class="notification-details">
                От: ${notification.senderName}<br>
                Компания: ${notification.companyName}<br>
                Дата: ${formatDate(notification.sendDate)}
            </p>
            <div class="notification-actions">
                <button onclick="acceptRequest(${notification.id})">Принять</button>
                <button onclick="rejectRequest(${notification.id})">Отклонить</button>
            </div>
        </div>
    `;
    
    return div;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU');
}

// function acceptRequest(notificationId) {
//     // Добавим позже
// }

// function rejectRequest(notificationId) {
//     // Добавим позже
// }