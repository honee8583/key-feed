package com.leedahun.notificationservice.common.response;

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
public class CommonPageResponse<T> {
    private List<T> content;
    private Long nextCursorId;
    private boolean hasNext;
}
