package com.cfi.lookout.loos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by subhgupt on 3/18/16.
 */

/*
* {
				id: self.id,
				address: self.address,
				latitude: self.latitude,
				longitude: self.longitude,
				timing: self.timing,
				type: self.type,
				urinal_count: self.urinal_count,
				handicap_support: self.handicap_support,
				paid: self.paid,
				avg_rating: self.avg_rating,
				picture_url: self.picture_url,
				created_at: self.created_at,
				updated_at: self.updated_at
		}
* */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Loo {

    public Loo()
    {
        address = "";
        latitude = 0.0f;
        longitude = 0.0f;
        timing = "";
        type = "";
        handicapSupport = false;
        paid = false;
        urinalCount = 0;
        avgRating = 2.5f;
        pictureUrl = "";
    }

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("address")
    private String address;

    @JsonProperty("latitude")
    private Float latitude;

    @JsonProperty("longitude")
    private Float longitude;

    @JsonProperty("timing")
    private String timing;

    @JsonProperty("type")
    private String type;

    @JsonProperty("handicap_support")
    private Boolean handicapSupport;

    @JsonProperty("paid")
    private Boolean paid;

    @JsonProperty("urinal_count")
    private Integer urinalCount;

    @JsonProperty("avg_rating")
    private Float avgRating;

    @JsonProperty("picture_url")
    private String pictureUrl;


}
