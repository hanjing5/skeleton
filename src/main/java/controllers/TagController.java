package controllers;

import api.CreateReceiptRequest;
import api.CreateTagRequest;
import api.ReceiptResponse;
import api.TagResponse;
import dao.ReceiptDao;
import dao.TagDao;
import generated.tables.records.ReceiptsRecord;
import generated.tables.records.TagsRecord;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("")
// @Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TagController {
    final TagDao tags;

    public TagController(TagDao tags) {
        this.tags = tags;
    }

	@PUT
	@Path("/tags/{tag}")
	public void toggleTag(@PathParam("tag") String tagName, Integer body) {
		TagsRecord foo = tags.findByReceiptidTag(body, tagName);
		System.out.println(foo);
		System.out.println(foo == null);
		if (foo == null) {
			tags.insert(body, tagName);
		} else {
			tags.delete(body, tagName);
		}
	}

    @GET
    @Path("/tags/{tag}")
    public List<TagResponse> getTags(@PathParam("tag") String tagName) {
       	List<TagsRecord> tagRecords = tags.getAllByTags(tagName);
        return tagRecords.stream().map(TagResponse::new).collect(toList());
    }

    @GET
    @Path("/tags")
    public List<TagResponse> getTags() {
       	List<TagsRecord> tagRecords = tags.getAllTags();
        return tagRecords.stream().map(TagResponse::new).collect(toList());
    }
}
