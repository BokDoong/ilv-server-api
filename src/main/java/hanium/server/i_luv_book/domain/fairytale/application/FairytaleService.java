package hanium.server.i_luv_book.domain.fairytale.application;


import hanium.server.i_luv_book.domain.fairytale.dao.TMPChildRepository;
import hanium.server.i_luv_book.domain.fairytale.domain.Fairytale;
import hanium.server.i_luv_book.domain.fairytale.domain.enums.FariyTaleDifficulty;
import hanium.server.i_luv_book.domain.fairytale.dto.request.GameFairyTaleRequestDTO;
import hanium.server.i_luv_book.domain.fairytale.dto.request.GeneralFairyTaleRequestDTO;
import hanium.server.i_luv_book.domain.fairytale.dto.response.GameFairyTaleSelectionResponseDTO;
import hanium.server.i_luv_book.domain.fairytale.dto.response.GeneralFairyTaleResponseDTO;
import hanium.server.i_luv_book.domain.user.application.UserCommandService;
import hanium.server.i_luv_book.domain.user.domain.Child;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class FairytaleService {

    private final FairytaleWebClientService fairytaleWebClientService;
    private final FairytalePersistentService fairytalePersistentService;

    public GeneralFairyTaleResponseDTO createAndSaveGeneralTale(GeneralFairyTaleRequestDTO taleRequestDTO,Long childId) {
        GeneralFairyTaleResponseDTO taleResponseDTO = fairytaleWebClientService.createGeneralTale(taleRequestDTO).block();
        Fairytale fairytale = fairytalePersistentService.saveFairytaleWithAllPage(taleRequestDTO,taleResponseDTO,childId);
        fairytalePersistentService.saveFairytaleKeyword(fairytale,taleRequestDTO);
        return taleResponseDTO;
    }



//    public GeneralFairyTaleResponseDTO createAndSaveGameTale(GameFairyTaleRequestDTO taleRequestDTO, Long childId) {
//        //선택지가 있다면 기존의 동화가 있다는 것.
//        if (StringUtils.hasText(taleRequestDTO.getSelection())) {
//
//        } else {
//            // 기존의 동화가 없는 경우
//            GameFairyTaleSelectionResponseDTO taleResponseDTO = fairytaleWebClientService.createGameTale(,taleRequestDTO.getDifficulty().getGameUrl());
//        }
//    }
}