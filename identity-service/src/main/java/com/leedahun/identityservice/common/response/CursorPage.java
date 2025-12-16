package com.leedahun.identityservice.common.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class CursorPage<T> {
    private List<T> content;
    private Long nextCursorId;
    private boolean hasNext;

    public CursorPage(List<T> content, Long nextCursorId, boolean hasNext) {
        this.content = content;
        this.nextCursorId = nextCursorId;
        this.hasNext = hasNext;
    }
}
