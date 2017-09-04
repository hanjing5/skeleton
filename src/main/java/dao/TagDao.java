package dao;

import api.ReceiptResponse;
import generated.tables.records.TagsRecord;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static generated.Tables.TAGS;

public class TagDao {
    DSLContext dsl;

    public TagDao(Configuration jooqConfig) {
        this.dsl = DSL.using(jooqConfig);
    }

    public int insert(Integer receiptId, String tag) {
        TagsRecord tagsRecord = dsl
                .insertInto(TAGS, TAGS.RECEIPT_ID, TAGS.TAG)
                .values(receiptId, tag)             
                .returning(TAGS.ID)
                .fetchOne();

        checkState(tagsRecord != null && tagsRecord.getId() != null, "Insert failed");

        return tagsRecord.getId();
    }

    public void delete(Integer receiptId, String tag) {
        dsl.delete(TAGS)
            .where(TAGS.RECEIPT_ID.eq(receiptId), TAGS.TAG.eq(tag))
            .execute();
    }

    public TagsRecord findByReceiptidTag(Integer receiptId, String tag) {
        return dsl
                .selectFrom(TAGS)
                .where(TAGS.RECEIPT_ID.eq(receiptId), TAGS.TAG.eq(tag))
                .fetchOne();
    }

    public List<TagsRecord> getAllByTags(String tag) {
        return dsl
                .selectFrom(TAGS)
                .where(TAGS.TAG.eq(tag))
                .fetch();
    }

    public List<TagsRecord> getAllTags() {
        return dsl.selectFrom(TAGS).fetch();
    }
}
