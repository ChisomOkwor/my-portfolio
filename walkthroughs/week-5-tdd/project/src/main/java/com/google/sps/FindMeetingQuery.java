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
import java.util.Set;
import java.util.Collections;
import java.util.Iterator;
import java.util.Arrays;


public final class FindMeetingQuery {

    private final ArrayList < TimeRange > options = new ArrayList < TimeRange > ();

    public Collection < TimeRange > query(Collection < Event > events, MeetingRequest request) {

        Collection attendees = request.getAttendees();
        Collection optionalAttendees = request.getOptionalAttendees();

        // check if duration is longer than the day. if so, return empty collection.
        long duration = request.getDuration();
        if (duration > TimeRange.WHOLE_DAY.duration()) {
            return Arrays.asList();
        }

        // if no attendees or optional attendees return collection of one item being the full day
        if (attendees.isEmpty() && optionalAttendees.isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);

            // if there are optional attendees but no attendees, optional attendees will be treated as attendees
        } else if (attendees.isEmpty()) {
            attendees = optionalAttendees;
        }

        // Sorts array of event objects by start time in ascending order
        ArrayList < Event > eventsList = new ArrayList < Event > (events);
        Collections.sort(eventsList, Event.ORDER_BY_START_TIME);

        Iterator < Event > eventsListIterator = eventsList.iterator();

        // if no events return the whole day
        if (eventsListIterator.hasNext() == false) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }

        // Conflicting event is found
        Event conflict = eventsListIterator.next();
        int start;

        // Ignore conflict if it doesn't involve any person in the request 
        if (conflictIsRelevant(conflict, attendees) == false) {
            start = TimeRange.START_OF_DAY;
        } else {

            int possibleDurationStart = TimeRange.START_OF_DAY;
            int possibleDurationEnd = conflict.getWhen().start();
            if (slotDurationIsPossible(request, possibleDurationStart, possibleDurationEnd)) {
                options.add(TimeRange.fromStartEnd(possibleDurationStart, possibleDurationEnd, false));
            }

            // start begins at the end of first conflict
            start = conflict.getWhen().end();
        }

        while (eventsListIterator.hasNext()) {
            Event prevConflict = conflict;
            conflict = eventsListIterator.next();

            // if previous conflict end is after current conflict end, replace current conflict with previous conflict
            if (prevConflict.getWhen().end() > conflict.getWhen().end()) {
                conflict = prevConflict;
            }

            if (conflictIsRelevant(conflict, attendees) || (!options.isEmpty() && conflictIsRelevantForOptionalMembers(conflict, request))) {

                if (slotDurationIsPossible(request, start, conflict.getWhen().start())) {
                    TimeRange slot = TimeRange.fromStartEnd(start, conflict.getWhen().start(), false);
                    options.add(slot);
                }

                // set the start for the next option
                start = conflict.getWhen().end();
            }
        }

        // check if last conflict is relevant. If yes, add option between last conflict and end of day
        if (conflictIsRelevant(conflict, attendees) == false) {
            options.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
            return options;
        }

        // check if last conflict is at end of day. If no, can add option btwn last conflict and end of day.
        if ((conflict.getWhen().contains(TimeRange.END_OF_DAY)) == false) {
            if (slotDurationIsPossible(request, start, TimeRange.END_OF_DAY)) {
                options.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
            }
        }
        return options;
    }


    private boolean slotDurationIsPossible(MeetingRequest request, int startSlot, int endSlot) {
        return (request.getDuration() <= (endSlot - startSlot));
    }

    private boolean conflictIsRelevant(Event conflict, Collection meetingAttendees) {

        //  Returns true if the conflict involves meetingAttendees 
        // i.e if the conflicting event have attendees common to meetingAttendees

        Set < String > conflictAttendees = conflict.getAttendees();
        return !(Collections.disjoint(conflictAttendees, meetingAttendees));
    }

    private boolean conflictIsRelevantForOptionalMembers(Event conflict, MeetingRequest request) {
        Set < String > conflictAttendees = conflict.getAttendees();
        Collection < String > optionalAttendees = request.getOptionalAttendees();

        // return false if optional attendee has whole day event
        if (conflict.getWhen().start() == TimeRange.START_OF_DAY && conflict.getWhen().end() == TimeRange.END_OF_DAY) {
            return false;
        }

        return !(Collections.disjoint(conflictAttendees, optionalAttendees));

    }
}