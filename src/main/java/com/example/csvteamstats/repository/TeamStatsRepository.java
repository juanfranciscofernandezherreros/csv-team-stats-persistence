package com.example.csvteamstats.repository;
import com.example.csvteamstats.entity.TeamStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TeamStatsRepository extends JpaRepository<TeamStats,Long> {
 Optional<TeamStats> findByMatchIdAndPeriodAndCategoryAndMetric(String matchId,String period,String category,String metric);
}
