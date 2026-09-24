package com.example.csvteamstats.entity;
import jakarta.persistence.*;
@Entity
@Table(name="team_stats",uniqueConstraints=@UniqueConstraint(name="uk_team_stats_match_period_category_metric",columnNames={"match_id","period","category","metric"}))
public class TeamStats {
 @Id @SequenceGenerator(name="team_stats_seq_gen",sequenceName="team_stats_seq",allocationSize=100)
 @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="team_stats_seq_gen")
 private Long id;
 @Column(name="match_id",nullable=false) private String matchId;
 @Column(nullable=false) private String period;
 @Column(nullable=false) private String category;
 @Column(nullable=false) private String metric;
 @Column(name="home_team",nullable=false) private String homeTeam;
 @Column(name="home_value") private String homeValue;
 @Column(name="away_team",nullable=false) private String awayTeam;
 @Column(name="away_value") private String awayValue;
 @Column(name="source_url",columnDefinition="text") private String sourceUrl;
 @Column(name="source_event_id",nullable=false) private String sourceEventId;
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public String getMatchId(){return matchId;} public void setMatchId(String v){matchId=v;}
 public String getPeriod(){return period;} public void setPeriod(String v){period=v;}
 public String getCategory(){return category;} public void setCategory(String v){category=v;}
 public String getMetric(){return metric;} public void setMetric(String v){metric=v;}
 public String getHomeTeam(){return homeTeam;} public void setHomeTeam(String v){homeTeam=v;}
 public String getHomeValue(){return homeValue;} public void setHomeValue(String v){homeValue=v;}
 public String getAwayTeam(){return awayTeam;} public void setAwayTeam(String v){awayTeam=v;}
 public String getAwayValue(){return awayValue;} public void setAwayValue(String v){awayValue=v;}
 public String getSourceUrl(){return sourceUrl;} public void setSourceUrl(String v){sourceUrl=v;}
 public String getSourceEventId(){return sourceEventId;} public void setSourceEventId(String v){sourceEventId=v;}
}
