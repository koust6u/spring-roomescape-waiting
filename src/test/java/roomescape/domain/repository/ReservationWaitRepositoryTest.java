package roomescape.domain.repository;

import static roomescape.domain.ReservationStatus.RESERVED;
import static roomescape.domain.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.Theme;

@Transactional
@SpringBootTest
class ReservationWaitRepositoryTest {
    @Autowired
    private ReservationWaitRepository waitRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    private final Member dummyMember = new Member("aa", "aa@aa.aa", "aa");
    private final Theme dummyTheme = new Theme("n", "d", "t");
    private final ReservationTime dummyTime = new ReservationTime(LocalTime.of(1, 0));
    private final Reservation dummyReservation =
            new Reservation(LocalDate.of(2023, 12, 11), dummyTime, dummyTheme);

    @BeforeEach
    void setUp() {
        memberRepository.save(dummyMember);
        themeRepository.save(dummyTheme);
        timeRepository.save(dummyTime);
        reservationRepository.save(dummyReservation);
    }

    @Test
    @DisplayName("예약 대기 정보를 저장한다")
    void save_ShouldStoreReservationWaitInfo() {
        // given
        ReservationWait reservationWait = new ReservationWait(dummyMember, dummyReservation, 0, WAITING);

        // when
        waitRepository.save(reservationWait);

        // then
        Assertions.assertThat(waitRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("예약 대기 정보를 예약 상태와 예약으로 조회한다")
    void findByPeriodAndMemberAndTheme_ShouldGetSpecificPersistence() {
        // given
        Reservation reservation1 = new Reservation(LocalDate.of(2017, 3, 14), dummyTime, dummyTheme);
        Reservation reservation2 = new Reservation(LocalDate.of(2017, 3, 31), dummyTime, dummyTheme);
        Reservation reservation3 = new Reservation(LocalDate.of(2017, 4, 2), dummyTime, dummyTheme);

        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);

        ReservationWait reservationWait1 = new ReservationWait(dummyMember, reservation1, 0, RESERVED);
        ReservationWait reservationWait2 = new ReservationWait(dummyMember, reservation2, 0, WAITING);
        ReservationWait reservationWait3 = new ReservationWait(dummyMember, reservation3, 0, WAITING);

        waitRepository.save(reservationWait1);
        waitRepository.save(reservationWait2);
        waitRepository.save(reservationWait3);

        // when
        List<ReservationWait> wait = waitRepository.findByPeriodAndMemberAndThemeAndStatus(
                LocalDate.of(2017, 3, 13),
                LocalDate.of(2017, 4, 1),
                dummyMember.getName(),
                dummyTheme.getName(),
                RESERVED);

        // then
        Assertions.assertThat(wait).hasSize(1)
                .containsExactlyInAnyOrder(wait.get(0));
    }

    @Test
    @DisplayName("회원과 예약 대기 정보를 바탕으로 예약대기 정보를 가져온다")
    void findAllByMemberAndStatus_ShouldGetSpecificWaitInfos() {
        // given
        Member member1 = new Member("aa", "aa@aa.aa", "aa");
        Member member2 = new Member("bb", "bb@bb.bb", "bb");

        memberRepository.save(member1);
        memberRepository.save(member2);

        ReservationWait wait1 = new ReservationWait(member1, dummyReservation, 0, WAITING);
        ReservationWait wait2 = new ReservationWait(member2, dummyReservation, 0, WAITING);
        ReservationWait wait3 = new ReservationWait(member2, dummyReservation, 0, RESERVED);

        waitRepository.save(wait1);
        waitRepository.save(wait2);
        waitRepository.save(wait3);

        // when
        List<ReservationWait> waits = waitRepository.findAllByMemberAndStatus(member2, WAITING);

        // then
        Assertions.assertThat(waits).hasSize(1)
                .containsExactlyInAnyOrder(wait2);
    }

    @Test
    @DisplayName("멤버의 ID로 예약 대기 상태를 찾는다.")
    void findByMemberIdAndStatus_ShouldGetSpecificReservationWait() {
        // given
        Member member1 = new Member("aa", "aa@aa.aa", "aa");
        Member member2 = new Member("bb", "bb@bb.bb", "bb");

        memberRepository.save(member1);
        memberRepository.save(member2);

        ReservationWait wait1 = new ReservationWait(member1, dummyReservation, 0, RESERVED);
        ReservationWait wait2 = new ReservationWait(member1, dummyReservation, 0, WAITING);
        ReservationWait wait3 = new ReservationWait(member2, dummyReservation, 0, RESERVED);

        waitRepository.save(wait1);
        waitRepository.save(wait2);
        waitRepository.save(wait3);

        // when
        List<ReservationWait> waits = waitRepository.findByMemberIdAndStatus(member1.getId(), RESERVED);

        // then
        Assertions.assertThat(waits).hasSize(1)
                .containsExactlyInAnyOrder(wait1);
    }

    @Test
    @DisplayName("멤버의 ID로 예약 대기 정보를 삭제할 수 있다")
    void deleteByMemberId_ShouldRemovePersistence() {
        // given
        ReservationWait reservationWait = new ReservationWait(dummyMember, dummyReservation, 0, WAITING);
        waitRepository.save(reservationWait);

        // when
        waitRepository.deleteByMemberId(dummyMember.getId());

        // then
        Assertions.assertThat(waitRepository.findAll())
                .isEmpty();
    }


    @Test
    @DisplayName("예약의 ID로 예약 대기 정보를 삭제할 수 있다")
    void deleteByReservationId_ShouldRemovePersistence() {
        // given
        ReservationWait reservationWait = new ReservationWait(dummyMember, dummyReservation, 0, WAITING);
        waitRepository.save(reservationWait);

        // when
        waitRepository.deleteByReservationId(dummyReservation.getId());

        // then
        Assertions.assertThat(waitRepository.findAll())
                .isEmpty();
    }
}
