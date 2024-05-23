package roomescape.service.dto.response.member;

public record MemberResponse(Long id, String name) {

    public MemberResponse(String name) {
        this(null, name);
    }
}
