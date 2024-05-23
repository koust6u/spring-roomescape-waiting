package roomescape.service.dto.response.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import roomescape.domain.Reservation;

public record UserReservationResponse(
        @NotNull(message = "예약 아이디는 빈값이 올 수 없습니다.") Long reservationId,
        @NotBlank(message = "예약 테마는 빈값이 올 수 없습니다.") String theme,
        @NotNull(message = "예약 일자는 빈값이 올 수 없습니다.") LocalDate date,
        @NotNull(message = "예약 시간은 빈값이 올 수 없습니다.") LocalTime time,
        @NotBlank(message = "예약 상태는 빈값이 올 수 없습니다.") String status) {
    public static UserReservationResponse from(Reservation reservation) {
        return new UserReservationResponse(reservation.getId(), reservation.getTheme().getName(), reservation.getDate(),
                reservation.getTime().getStartAt(), "예약");
    }
}
