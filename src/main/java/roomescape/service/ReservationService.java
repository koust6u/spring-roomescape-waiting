package roomescape.service;

import static roomescape.domain.ReservationStatus.RESERVED;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.Theme;
import roomescape.domain.policy.CurrentDueTimePolicy;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ReservationWaitRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.reservation.ReservationSearchCond;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.UserReservationResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitRepository waitRepository;

    public List<ReservationResponse> findAllReservation() {
        List<ReservationWait> waits = waitRepository.findAll();

        return waits.stream()
                .map(wait -> ReservationResponse.from(wait.getReservation(), wait.getMember()))
                .toList();
    }

    public List<ReservationResponse> findAllReservationByConditions(ReservationSearchCond cond) {
        return waitRepository.findByPeriodAndMemberAndThemeAndStatus(cond.start(), cond.end(), cond.memberName(),
                        cond.themeName(), RESERVED)
                .stream()
                .map(ReservationWait::getReservation)
                .map(reservation -> ReservationResponse.from(reservation, cond.memberName()))
                .toList();
    }

    public List<UserReservationResponse> findAllByMemberId(Long memberId) {
        return waitRepository.findByMemberIdAndStatus(memberId, RESERVED)
                .stream()
                .map(ReservationWait::getReservation)
                .map(UserReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse saveReservation(ReservationRequest request) {
        ReservationTime time = findReservationTimeById(request.timeId());
        Theme theme = findThemeById(request.themeId());
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(AuthenticationFailureException::new);
        Reservation verifiedReservation = verifyReservation(request, time, theme);
        Reservation savedReservation = reservationRepository.save(verifiedReservation);

        waitRepository.save(new ReservationWait(member, savedReservation, 0));
        return ReservationResponse.from(savedReservation, member);
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }

    private ReservationTime findReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }

    private Reservation verifyReservation(ReservationRequest request, ReservationTime time, Theme theme) {
        Reservation reservation = request.toReservation(time, theme);
        List<Reservation> findReservations = reservationRepository.findByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId());
        reservation.validateDateTimeReservation(new CurrentDueTimePolicy());
        reservation.validateDuplicateDateTime(findReservations);
        return reservation;
    }

    @Transactional
    public void deleteReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        waitRepository.deleteByReservationId(reservationId);
        reservationRepository.delete(reservation);
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }
}
