// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Comparator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Arrays;

public final class FindMeetingQuery {

    private final ArrayList < TimeRange > options = new ArrayList < TimeRange > ();

    public Collection < TimeRange > query(Collection < Event > events, MeetingRequest request) {

        Collection attendees = request.getAttendees();
        Collection optionalAttendees = request.getOptionalAttendees();

        // Check if duration is longer than the day. if so, return empty collection.
        long duration = request.getDuration();
        if (duration > TimeRange.WHOLE_DAY.duration()) {
            return Arrays.asList();
        }

        // If no attendees or optional attendees return collection of one item being the full day
        if (attendees.isEmpty() && optionalAttendees.isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);

        // If there are optional attendees but no attendees, optional attendees will be treated as attendees
        } else if (attendees.isEmpty()) {
            attendees = optionalAttendees;
        }

        // If no events return the whole day
        if (events.isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }

        // Sorts array of event objects by start time in ascending order
        ArrayList < Event > eventsList = new ArrayList < Event > (events);
        Collections.sort(eventsList, Event.ORDER_BY_START_TIME);
        Iterator < Event > eventsListIterator = eventsList.iterator();

        // Initializing variables used in eventsList iterator loop
        int start = TimeRange.START_OF_DAY;
        Event event = null;
        Event prevEvent = null;

        while (eventsListIterator.hasNext()) {
            // If previous event end is after current event end, replace current event with previous event

            event = eventsListIterator.next();
            if (prevEvent != null) {
                if (prevEvent.getWhen().end() > event.getWhen().end()) {
                    event = prevEvent;
                }
            }
            
            if (isEventRelevant(event, attendees) || (!options.isEmpty() && isEventRelevantForOptionalMembers(event, request))) {
                if (isDurationPossible(request, start, event.getWhen().start())) {
                    TimeRange slot = TimeRange.fromStartEnd(start, event.getWhen().start(), false);
                    options.add(slot);
                }
                // Set the start for the next option
                start = event.getWhen().end();
            }

            prevEvent = event;
            
            // Adds duration between last event and end of the day
            if (!eventsListIterator.hasNext()) {
                if (isDurationPossible(request, start, TimeRange.END_OF_DAY)) {
                    options.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
                }
            }
        }

        return options;
    }

    private boolean isDurationPossible(MeetingRequest request, int startSlot, int endSlot) {
        return (request.getDuration() <= (endSlot - startSlot));
    }

    private boolean isEventRelevant(Event event, Collection meetingAttendees) {

        //  Returns true if the event involves meetingAttendees 
        // i.e if the conflicting event have attendees common to meetingAttendees

        Collection < String > eventAttendees = event.getAttendees();
        return !(Collections.disjoint(eventAttendees, meetingAttendees));
    }

    private boolean isEventRelevantForOptionalMembers(Event event, MeetingRequest request) {
        Collection < String > eventAttendees = event.getAttendees();
        Collection < String > optionalAttendees = request.getOptionalAttendees();

        // return false if optional attendee has whole day event
        if (event.getWhen().start() == TimeRange.START_OF_DAY && event.getWhen().end() == TimeRange.END_OF_DAY) {
            return false;
        }

        return !(Collections.disjoint(eventAttendees, optionalAttendees));

    }
}