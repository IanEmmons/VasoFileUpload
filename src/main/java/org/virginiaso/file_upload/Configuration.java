package org.virginiaso.file_upload;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.virginiaso.file_upload.util.FileUtil;
import org.virginiaso.file_upload.util.StringUtil;
import org.virginiaso.file_upload.yaml_dto.ConfigurationDto;
import org.virginiaso.file_upload.yaml_dto.EventDto;
import org.virginiaso.file_upload.yaml_dto.TimeIntervalDto;
import org.virginiaso.file_upload.yaml_dto.TournamentDto;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

final class Configuration {
	private static final Pattern SINGLE_INT_PATTERN = Pattern.compile(
		"[0-9]+");
	private static final Pattern RANGE_PATTERN = Pattern.compile(
		"([0-9]+)[ \t]*-[ \t]*([0-9]+)");

	private static ZoneId timeZone;

	private Configuration() {}	// prevents instantiation

	public static ZoneId getTimeZone() {
		return timeZone;
	}

	public static void setTimeZone(String timeZoneStr) {
		timeZone = ZoneId.of(timeZoneStr);
	}

	public static List<Tournament> parse(String tournamentConfigRsrc)
			throws IOException {
		try (var rdr = FileUtil.getResourceAsReader(tournamentConfigRsrc)) {
			var yaml = new Yaml(new Constructor(ConfigurationDto.class, new LoaderOptions()));
			ConfigurationDto configurationDto = yaml.load(rdr);
			return configurationDto.tournaments.stream()
				.map(Configuration::convertTournament)
				.collect(Collectors.toUnmodifiableList());
		}
	}

	private static Tournament convertTournament(TournamentDto tournamentDto) {
		var tournamentDate = LocalDate.parse(tournamentDto.date.trim());
		EnumMap<Division, Set<Integer>> teams = convertTeams(tournamentDto.teams);
		EnumMap<Event, EnumMap<Division, TimeInterval>> events = convertEvents(
			tournamentDto.events, tournamentDate);
		return new Tournament(tournamentDto.name, tournamentDate, teams, events);
	}

	private static EnumMap<Division, Set<Integer>> convertTeams(
			Map<String, String> teams) {
		return teams.entrySet().stream().collect(Collectors.toMap(
			entry -> Division.valueOf(entry.getKey()),	// key mapper
			entry -> convertTeamList(entry.getValue()),	// value mapper
			(v1, v2) -> {											// merge function
				throw new IllegalArgumentException("Duplicate key in teams map");
			},
			() -> new EnumMap<>(Division.class)));			// map factory
	}

	static Set<Integer> convertTeamList(String listDescriptor) {
		if (StringUtil.isBlank(listDescriptor)) {
			throw new IllegalArgumentException("Team list is empty");
		}
		return Stream.of(listDescriptor.split(Pattern.quote(",")))
			.map(String::trim)
			.map(Configuration::convertTeamListEntry)
			.flatMap(IntStream::boxed)
			.collect(Collectors.toUnmodifiableSet());
	}

	private static IntStream convertTeamListEntry(String entryDescriptor) {
		if (SINGLE_INT_PATTERN.matcher(entryDescriptor).matches()) {
			return IntStream.of(Integer.parseInt(entryDescriptor));
		} else {
			var m = RANGE_PATTERN.matcher(entryDescriptor);
			if (!m.matches()) {
				throw new IllegalArgumentException(String.format(
					"Team list entry '%1$s' is malformed", entryDescriptor));
			}
			var low = Integer.parseInt(m.group(1));
			var high = Integer.parseInt(m.group(2));
			return IntStream.rangeClosed(low, high);
		}
	}

	private static EnumMap<Event, EnumMap<Division, TimeInterval>> convertEvents(
			List<EventDto> events, LocalDate tournamentDate) {
		return events.stream().collect(Collectors.toMap(
			eventDto -> Event.valueOf(eventDto.name),						// key mapper
			eventDto -> convertIntervals(eventDto, tournamentDate),	// value mapper
			(v1, v2) -> {															// merge function
				throw new IllegalArgumentException("Duplicate key in events map");
			},
			() -> new EnumMap<>(Event.class)));								// map factory
	}

	private static EnumMap<Division, TimeInterval> convertIntervals(EventDto eventDto,
			LocalDate tournamentDate) {
		EnumMap<Division, TimeInterval> result = new EnumMap<>(Division.class);
		if (eventDto.BC != null) {
			if (eventDto.A != null || eventDto.B != null || eventDto.C != null) {
				throw new IllegalArgumentException(String.format(
					"Event '%1$s' cannot have an A, B, or C time interval if it has a BC one",
					eventDto.name));
			}
			var ti = convertInterval(eventDto.BC, tournamentDate);
			result.put(Division.B, ti);
			result.put(Division.C, ti);
		} else if (eventDto.A != null) {
			if (eventDto.B != null || eventDto.C != null || eventDto.BC != null) {
				throw new IllegalArgumentException(String.format(
					"Event '%1$s' cannot have a B or C time interval if it has an A one",
					eventDto.name));
			}
			result.put(Division.A, convertInterval(eventDto.A, tournamentDate));
		} else if (eventDto.B == null && eventDto.C == null) {
			throw new IllegalArgumentException(String.format(
				"Event '%1$s' has no time interval", eventDto.name));
		} else {
			if (eventDto.B != null) {
				result.put(Division.B, convertInterval(eventDto.B, tournamentDate));
			}
			if (eventDto.C != null) {
				result.put(Division.C, convertInterval(eventDto.C, tournamentDate));
			}
		}
		return result;
	}

	private static TimeInterval convertInterval(TimeIntervalDto intervalDto,
		LocalDate tournamentDate) {
		return (StringUtil.isBlank(intervalDto.from) || StringUtil.isBlank(intervalDto.to))
			? new TimeInterval(tournamentDate)
			: new TimeInterval(intervalDto.from, intervalDto.to);
	}
}
