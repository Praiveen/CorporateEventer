class CurrentUserMeetings {
    constructor() {
        this.loadUserMeetings();
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        document.addEventListener('click', async (e) => {
            if (e.target.classList.contains('btn-delete-meeting')) {
                const meetingId = e.target.dataset.meetingId;
                if (confirm('Вы уверены, что хотите удалить это мероприятие?')) {
                    await this.deleteMeeting(meetingId);
                }
            }
            if (e.target.classList.contains('btn-complete-meeting')) {
                const meetingId = e.target.dataset.meetingId;
                if (confirm('Вы уверены, что хотите завершить это мероприятие?')) {
                    await this.completeMeeting(meetingId);
                }
            }
        });
    }

    async deleteMeeting(meetingId) {
        try {
            const response = await fetch(`/event/delete-meeting/${meetingId}`, {
                method: 'POST'
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }

            this.loadUserMeetings();
        } catch (error) {
            console.error('Error deleting meeting:', error);
            alert('Ошибка при удалении мероприятия: ' + error.message);
        }
    }

    async completeMeeting(meetingId) {
        try {
            const response = await fetch(`/event/complete-meeting/${meetingId}`, {
                method: 'POST'
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }

            this.loadUserMeetings();
        } catch (error) {
            console.error('Error completing meeting:', error);
            alert('Ошибка при завершении мероприятия: ' + error.message);
        }
    }

    async loadUserMeetings() {
        try {
            const response = await fetch('/event/user-meetings');
            if (!response.ok) throw new Error('Ошибка загрузки событий');
            
            const events = await response.json();
            this.renderMeetings(events);
            
            this.updateCalendarMeetings(events);
        } catch (error) {
            console.error('Error loading meetings:', error);
        }
    }

    updateCalendarMeetings(events) {
        const calendarTasks = window.calendarTasks || {};
        
        events.currentEvents.forEach(event => {
            const date = new Date(event.startTime);
            const dateKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
            
            if (!calendarTasks[dateKey]) {
                calendarTasks[dateKey] = [];
            }
            calendarTasks[dateKey].push({
                title: event.title,
                type: 'meeting'
            });
        });
        
        window.calendarTasks = calendarTasks;
        
        if (window.refreshCalendar) {
            window.refreshCalendar();
        }
    }

    renderMeetings(events) {
        const currentEventsContainer = document.querySelector('.current-meetings');
        const pastEventsContainer = document.querySelector('.past-meetings');
        
        if (currentEventsContainer) {
            currentEventsContainer.innerHTML = events.currentEvents.map(meeting => `
                <div class="event-card ${meeting.isOrganizer ? 'organizer-meeting' : ''}">
                    <h3>${meeting.title}</h3>
                    <p>${meeting.description || 'Нет описания'}</p>
                    <div class="event-details">
                        <span><i class="fas fa-calendar"></i> ${this.formatDate(meeting.startTime)}</span>
                        <span><i class="fas fa-clock"></i> ${this.formatTime(meeting.startTime)} - ${this.formatTime(meeting.endTime)}</span>
                    </div>
                    <div class="event-status ${meeting.status.toLowerCase()}">${meeting.status}</div>
                    <div class="event-creator">
                        ${meeting.isOrganizer ? 'Вы организатор' : `Организатор: ${meeting.createdBy}`}
                    </div>
                    ${meeting.isOrganizer ? `
                        <div class="organizer-actions">
                            <button class="btn-complete-meeting" data-meeting-id="${meeting.meetingId}">
                                <i class="fas fa-check"></i> Завершить
                            </button>
                            <button class="btn-delete-meeting" data-meeting-id="${meeting.meetingId}">
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
                    <h3>${event.title}</h3>
                    <p>${event.description || 'Нет описания'}</p>
                    <div class="event-details">
                        <span><i class="fas fa-calendar"></i> ${this.formatDate(event.startTime)}</span>
                        <span><i class="fas fa-clock"></i> ${this.formatTime(event.startTime)} - ${this.formatTime(event.endTime)}</span>
                        <span><i class="fas fa-map-marker-alt"></i> ${event.location || 'Место не указано'}</span>
                    </div>
                    <div class="event-status ${event.status.toLowerCase()}">${event.status}</div>
                    <div class="event-creator">${event.isOrganizer ? 'Вы  организатор' : `Создал: ${event.isOrganizer}`}</div>
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