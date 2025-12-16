package com.leedahun.identityservice.common.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorPage<T> {
    private List<T> content;
    private Long nextCursorId;
    private boolean hasNext;

    public CursorPage(List<T> content, boolean hasNext, Long nextCursorId) {
        this.content = content;
        this.hasNext = hasNext;
        this.nextCursorId = nextCursorId;
    }
}
