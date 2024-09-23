package com.omegafrog.My.piano.app.web.domain.order;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SellableItemFactory {
    private final LessonRepository lessonRepository;

    private final SheetPostRepository sheetPostRepository;

    /**
     * 요청의 mainResource와 주어진 id에 따라 맞는 객체를 검색해 반환하는 함수
     *
     * @param mainResourceName 구매 요청에 해당하는 물품의 종류.
     *                         ("/{mainResource}/buy")
     * @param id               구매 요청에 해당하는 물품의 id
     * @return SellableItem의 서브클래스인 요청에 맞는 물품 엔티티
     * @throws EntityNotFoundException 찾으려는 객체 엔티티가 존재하지 않는 경우
     */
    public SellableItem createDetailedItem(String mainResourceName, Long id) {
        return switch (mainResourceName) {
            case ("lessons") -> lessonRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find" + mainResourceName + " entity : "
                            + id));
            case ("sheet") -> sheetPostRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find" + mainResourceName + " entity : "
                            + id));
            default -> throw new IllegalArgumentException("Wrong mainResource. " + mainResourceName);
        };
    }
}
