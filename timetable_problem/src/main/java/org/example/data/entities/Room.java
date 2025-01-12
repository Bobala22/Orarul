package org.example.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Room {


    @JsonProperty("name")
    private final String name;
    @JsonProperty("isCourseRoom")
    private final boolean isCourseRoom;

    public Room(String name, boolean isCourseRoom) {
        this.name = name;
        this.isCourseRoom = isCourseRoom;
    }

    public String getName() {
        return name;
    }

    public boolean isCourseRoom() {
        return isCourseRoom;
    }
}
