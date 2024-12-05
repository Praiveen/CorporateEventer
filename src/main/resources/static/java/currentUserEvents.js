class CurrentUserEvents {
    constructor() {
        this.loadUserEvents();
    }

    async loadUserEvents() {
        try {
            const response = await fetch('/event/user-events');
            if (!response.ok) throw new Error('Ошибка загрузки событий');
            
            const events = await response.json();
            this.renderEvents(events);
            
            // Добавляем события в календарь
            this.updateCalendarEvents(events);
        } catch (error) {
            console.error('Error loading events:', error);
        }
    }

    updateCalendarEvents(events) {
        const calendarTasks = window.calendarTasks || {};
        
        events.currentEvents.forEach(event => {
            const date = new Date(event.startTime);
            const dateKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
            
            if (!calendarTasks[dateKey]) {
                calendarTasks[dateKey] = [];
            }
            calendarTasks[dateKey].push({
                title: event.title,
                type: 'event'
            });
        });
        
        window.calendarTasks = calendarTasks;
        
        if (window.refreshCalendar) {
            window.refreshCalendar();
        }
    }

    renderEvents(events) {
        const currentEventsContainer = document.querySelector('.current-events');
        const pastEventsContainer = document.querySelector('.past-events');
        
        if (currentEventsContainer) {
            currentEventsContainer.innerHTML = events.currentEvents.map(event => `
                <div class="event-card">
                    <h3>${event.title || 'NULL'}</h3>
                    <p>${event.description || 'Нет описания'}</p>
                    <div class="event-details">
                        <span>
                            <i class="fas fa-calendar"></i>
                            ${this.formatDate(event.startTime)}
                        </span>
                        <span>
                            <i class="fas fa-clock"></i>
                            ${this.formatTime(event.startTime)} - ${this.formatTime(event.endTime)}
                        </span>
                        <span>
                            <i class="fas fa-map-marker-alt"></i>
                            ${event.location || 'Место не указано'}
                        </span>
                    </div>
                    <div class="event-status ${event.status.toLowerCase()}">
                        ${event.status}
                    </div>
                    <div class="event-creator">
                        <i class="fas fa-user"></i>
                        Создал: ${event.createdBy || 'Система'}
                    </div>
                </div>
            `).join('');
        }
        
        if (pastEventsContainer) {
            pastEventsContainer.innerHTML = events.pastEvents.map(event => `
                <div class="event-card past">
                    <h3>${event.title || 'NULL'}</h3>
                    <p>${event.description || 'Нет описания'}</p>
                    <div class="event-details">
                        <span>
                            <i class="fas fa-calendar"></i>
                            ${this.formatDate(event.startTime)}
                        </span>
                        <span>
                            <i class="fas fa-clock"></i>
                            ${this.formatTime(event.startTime)} - ${this.formatTime(event.endTime)}
                        </span>
                        <span>
                            <i class="fas fa-map-marker-alt"></i>
                            ${event.location || 'Место не указано'}
                        </span>
                    </div>
                    <div class="event-creator">
                        <i class="fas fa-user"></i>
                        Создал: ${event.createdBy || 'Система'}
                    </div>
                </div>
            `).join('');
        }
    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU');
    }

    formatTime(dateString) {
        const date = new Date(dateString);
        return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const currentUserEvents = new CurrentUserEvents();
});