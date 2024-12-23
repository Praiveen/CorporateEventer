class CurrentUserEvents {
    constructor() {
        this.loadUserEvents();
        this.initializeEventListeners();
    }


    initializeEventListeners() {
        document.addEventListener('click', async (e) => {
            if (e.target.classList.contains('btn-delete-event')) {
                const eventId = e.target.dataset.eventId;
                if (confirm('Вы уверены, что хотите удалить это событие?')) {
                    await this.deleteEvent(eventId);
                }
            }

            if (e.target.classList.contains('btn-complete-event')) {
                const eventId = e.target.dataset.eventId;
                if (confirm('Вы уверены, что хотите завершить это событие?')) {
                    await this.completeEvent(eventId);
                }
            }
        });
    }

    async loadUserEvents() {
        try {
            const response = await fetch('/event/user-events');
            if (!response.ok) throw new Error('Ошибка загрузки событий');
            
            const events = await response.json();
            this.renderEvents(events);
            
            this.updateCalendarEvents(events);
        } catch (error) {
            console.error('Error loading events:', error);
        }
    }

    updateCalendarEvents(events) {
        window.calendarTasks = {};
        const calendarTasks = window.calendarTasks;
        
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
        
        if (window.refreshCalendar) {
            window.refreshCalendar();
        }
    }

    renderEvents(events) {
        const currentEventsContainer = document.querySelector('.current-events');
        const pastEventsContainer = document.querySelector('.past-events');
        
        if (currentEventsContainer) {
            currentEventsContainer.innerHTML = events.currentEvents.map(event => `
                <div class="event-card ${event.isCreator ? 'creator-event' : ''}">
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
                    
                    <div class="event-status ${event.status.toLowerCase()}">${event.status}</div>
                    <div class="event-creator">
                        <i class="fas fa-user"></i>
                        ${event.isCreator ? 'Вы создатель' : `Создал: ${event.createdBy}`}
                    </div>
                    ${event.isCreator ? `
                        <div class="creator-actions">
                            <button class="btn-complete-event" data-event-id="${event.eventId}">
                                <i class="fas fa-check"></i> Завершить
                            </button>
                            <button class="btn-delete-event" data-event-id="${event.eventId}">
                                <i class="fas fa-trash"></i> Удалить
                            </button>
                        </div>
                    ` : ''}
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
                    <div class="event-status ${event.status.toLowerCase()}">${event.status}</div>
                    <div class="event-creator">
                        <i class="fas fa-user"></i>
                        ${event.isCreator ? 'Вы создатель' : `Создал: ${event.createdBy}`}
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


    async deleteEvent(eventId) {
        try {
            const response = await fetch(`/event/delete-event/${eventId}`, {
                method: 'POST'
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }

            this.loadUserEvents();
        } catch (error) {
            console.error('Error deleting event:', error);
            alert('Ошибка при удалении события: ' + error.message);
        }
    }

    async completeEvent(eventId) {
        try {
            const response = await fetch(`/event/complete-event/${eventId}`, {
                method: 'POST'
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }

            this.loadUserEvents();
        } catch (error) {
            console.error('Error completing event:', error);
            alert('Ошибка при завершении события: ' + error.message);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const currentUserEvents = new CurrentUserEvents();
});