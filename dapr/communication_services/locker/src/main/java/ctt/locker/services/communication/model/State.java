package ctt.locker.services.communication.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Getter
@Setter
public class State {
    private String key;
    private Order value;
}
