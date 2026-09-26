package com.example.csvteamstats.consumer;

import com.example.csvteamstats.avro.TeamStatsKey;
import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.service.TeamStatsPersistenceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ParsedTeamStatsConsumerTest {

    @Test
    void forwardsWholeKafkaPollAsOnePersistenceBatch() {
        TeamStatsPersistenceService service = mock(TeamStatsPersistenceService.class);
        ParsedTeamStatsConsumer consumer = new ParsedTeamStatsConsumer(service);
        TeamStatsValue first = mock(TeamStatsValue.class);
        TeamStatsValue second = mock(TeamStatsValue.class);

        ConsumerRecord<TeamStatsKey, TeamStatsValue> firstRecord =
                new ConsumerRecord<>("team-stats.parsed", 0, 0L, null, first);
        ConsumerRecord<TeamStatsKey, TeamStatsValue> secondRecord =
                new ConsumerRecord<>("team-stats.parsed", 0, 1L, null, second);

        consumer.listen(List.of(firstRecord, secondRecord));

        verify(service).persistBatch(List.of(first, second));
    }
}
