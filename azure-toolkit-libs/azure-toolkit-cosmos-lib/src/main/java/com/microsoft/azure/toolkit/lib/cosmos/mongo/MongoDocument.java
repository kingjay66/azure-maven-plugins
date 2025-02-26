/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.cosmos.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.Deletable;
import com.microsoft.azure.toolkit.lib.common.utils.JsonUtils;
import com.microsoft.azure.toolkit.lib.cosmos.ICosmosDocument;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.cosmos.mongo.MongoDocumentModule.MONGO_ID_KEY;

public class MongoDocument extends AbstractAzResource<MongoDocument, MongoCollection, Document> implements Deletable, ICosmosDocument {
    protected MongoDocument(@NotNull String name, @NotNull String resourceGroupName, @NotNull AbstractAzResourceModule<MongoDocument, MongoCollection, Document> module) {
        super(name, resourceGroupName, module);
    }

    protected MongoDocument(@Nonnull MongoDocument origin) {
        super(origin);
    }

    @NotNull
    @Override
    public List<AbstractAzResourceModule<?, ?, ?>> getSubModules() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public Object getDocumentId() {
        return Optional.ofNullable(getRemote()).map(doc -> doc.get(MONGO_ID_KEY)).orElse(null);
    }

    @Nullable
    @Override
    public ObjectNode getDocument() {
        return Optional.ofNullable(getRemote()).map(Document::toJson)
                .map(json -> JsonUtils.fromJson(json, ObjectNode.class)).orElse(null);
    }

    @Override
    public void updateDocument(ObjectNode document) {
        final MongoDocumentDraft sqlDocumentDraft = (MongoDocumentDraft) this.update();
        sqlDocumentDraft.setDocument(document);
        sqlDocumentDraft.updateIfExist();
    }

    public String getSharedKey() {
        final String sharedKey = getParent().getSharedKey();
        final ObjectNode document = getDocument();
        return ObjectUtils.allNotNull(sharedKey, document) ? document.get(sharedKey).asText() : null;
    }

    @Override
    protected void setRemote(@javax.annotation.Nullable Document newRemote) {
        super.setRemote(newRemote);
    }

    @NotNull
    @Override
    public String loadStatus(@NotNull Document remote) {
        return Status.RUNNING;
    }
}
