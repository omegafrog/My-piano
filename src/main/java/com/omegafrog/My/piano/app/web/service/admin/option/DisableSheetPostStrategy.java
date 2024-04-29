package com.omegafrog.My.piano.app.web.service.admin.option;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DisableSheetPostStrategy implements SheetPostStrategy {

    public static final String OPTION_NAME = "disable";
    private final boolean optionValue;
    @Override
    public void update(SheetPost sheetPost) {
        if(optionValue)
            sheetPost.disable();
        else sheetPost.enable();
    }
}
