package nrw.heilmann.quarkus.bot.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Teamkill extends PanacheEntity {
	public Instant timestamp;
	public Long authorId;
	public Long affectedMemberId;
	public Integer amount;
	public Long guildId;
}

