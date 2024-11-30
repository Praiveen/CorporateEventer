class CurrentUserMeetings {
    constructor() {
        this.loadUserMeetings();
    }

    async loadUserMeetings() {
        try {
            const response = await fetch('/event/user-meetings');
            if (!response.ok) throw new Error('Ошибка загрузки событий');
            
            const events = await response.json();
            this.renderMeetings(events);
        } catch (error) {
            console.error('Error loading meetings:', error);
        }
    }

    renderMeetings(events) {
        const currentEventsContainer = document.querySelector('.current-meetings');
        const pastEventsContainer = document.querySelector('.past-meetings');
        
        if (currentEventsContainer) {
            currentEventsContainer.innerHTML = events.currentEvents.map(event => `
                <div class="event-card">
                    <h3>${event.title}</h3>
                    <p>${event.description || 'Нет описания'}</p>
                    <div class="event-details">
                        <span><i class="fas fa-calendar"></i> ${this.formatDate(event.startTime)}</span>
                        <span><i class="fas fa-clock"></i> ${this.formatTime(event.startTime)} - ${this.formatTime(event.endTime)}</span>
                        <span><i class="fas fa-map-marker-alt"></i> ${event.location || 'Место не указано'}</span>
                    </div>
                    <div class="event-status ${event.status.toLowerCase()}">${event.status}</div>
                    <div class="event-creator">Создал: ${event.createdBy}</div>
                </div>
            `).join('');
        }
        
        if (pastEventsContainer) {
            pastEventsContainer.innerHTML = events.pastEvents.map(event => `
                <div class="event-card past">
                    <h3>${event.title}</h3>
                    <p>${event.description || 'Нет описания'}</p>
                    <div class="event-details">
                        <span><i class="fas fa-calendar"></i> ${this.formatDate(event.startTime)}</span>
                        <span><i class="fas fa-clock"></i> ${this.formatTime(event.startTime)} - ${this.formatTime(event.endTime)}</span>
                        <span><i class="fas fa-map-marker-alt"></i> ${event.location || 'Место не указано'}</span>
                    </div>
                    <div class="event-creator">Создал: ${event.createdBy}</div>
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
    const currentUserMeetings = new CurrentUserMeetings();
});