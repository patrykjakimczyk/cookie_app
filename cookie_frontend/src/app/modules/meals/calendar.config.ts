import { CalendarOptions } from '@fullcalendar/core';
import timeGridPlugin from '@fullcalendar/timegrid';

export const calendarConfig: CalendarOptions = {
  initialView: 'timeGridWeek',
  plugins: [timeGridPlugin],
  headerToolbar: {
    left: 'title',
    right: 'prev,next',
  },
  allDaySlot: false,
  slotMinTime: '06:00:00',
  slotMaxTime: '23:00:00',
  slotLabelFormat: {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  },
  slotDuration: '01:00:00',
  expandRows: true,
  height: 600,
  dayHeaderFormat: {
    weekday: 'long',
    month: '2-digit',
    day: '2-digit',
    omitCommas: false,
  },
  firstDay: 1,
  nowIndicator: true,
};
