package com.omegafrog.My.piano.app.web.domain.order;

import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

public class SellableItemFactory {
    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private SheetPostRepository sheetPostRepository;

    public SellableItem getDetailedItem(String mainResourceName, Long id){
        return switch (mainResourceName) {
            case ("lesson") -> lessonRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find" + mainResourceName + " entity : "
                            + id));
            case ("sheet") -> sheetPostRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find" + mainResourceName + " entity : "
                            + id));
            default -> throw new IllegalArgumentException("Wrong mainResource. " + mainResourceName);
        };
    }
}
