/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.storage.share;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareFileItemProperties;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.Deletable;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Getter
public class ShareFile extends AbstractAzResource<ShareFile, IShareFile, ShareFileItem> implements Deletable, IShareFile {
    private final ShareFileModule subFileModule;

    protected ShareFile(@Nonnull String name, @Nonnull ShareFileModule module) {
        super(name, module);
        this.subFileModule = new ShareFileModule(this);
    }

    /**
     * copy constructor
     */
    public ShareFile(@Nonnull ShareFile origin) {
        super(origin);
        this.subFileModule = origin.subFileModule;
    }

    @Nonnull
    @Override
    public List<AbstractAzResourceModule<?, ?, ?>> getSubModules() {
        return this.isDirectory() ? Collections.singletonList(this.subFileModule) : Collections.emptyList();
    }

    @Nonnull
    @Override
    public String loadStatus(@Nonnull ShareFileItem remote) {
        return "OK";
    }

    public long getSize() {
        if (!this.isDirectory()) {
            return this.remoteOptional().map(ShareFileItem::getFileSize).orElse(-1L);
        }
        return -1;
    }

    @Nullable
    @Override
    public OffsetDateTime getLastModified() {
        return this.remoteOptional().map(ShareFileItem::getProperties).map(ShareFileItemProperties::getLastModified).orElse(null);
    }

    @Override
    @Nullable
    public OffsetDateTime getCreationTime() {
        return this.remoteOptional().map(ShareFileItem::getProperties).map(ShareFileItemProperties::getCreatedOn).orElse(null);
    }

    @Override
    public void download(OutputStream output) {
        if (!this.isDirectory()) {
            final ShareDirectoryClient parentClient = (ShareDirectoryClient) this.getParent().getClient();
            parentClient.getFileClient(this.getName()).download(output);
        }
    }

    @Override
    public void download(Path dest) {
        if (!this.isDirectory()) {
            final ShareDirectoryClient parentClient = (ShareDirectoryClient) this.getParent().getClient();
            parentClient.getFileClient(this.getName()).downloadToFile(dest.toAbsolutePath().toString());
        }
    }

    @Override
    public Object getClient() {
        final ShareDirectoryClient parentClient = (ShareDirectoryClient) this.getParent().getClient();
        return this.isDirectory() ? parentClient.getSubdirectoryClient(this.getName()) : parentClient.getFileClient(this.getName());
    }

    @Override
    public Share getShare() {
        return this.getParent().getShare();
    }

    @Override
    public boolean isDirectory() {
        return this.remoteOptional().map(ShareFileItem::isDirectory).orElse(false);
    }

    @Override
    public String getPath() {
        return Paths.get(this.getParent().getPath(), this.getName()).toString();
    }

    @Override
    public String getUrl() {
        final ShareDirectoryClient parentClient = (ShareDirectoryClient) this.getParent().getClient();
        return this.isDirectory() ? parentClient.getSubdirectoryClient(this.getName()).getDirectoryUrl() : parentClient.getFileClient(this.getName()).getFileUrl();
    }
}
