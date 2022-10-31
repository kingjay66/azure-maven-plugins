/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.storage.share;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.models.ShareProperties;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.Deletable;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Getter
public class Share extends AbstractAzResource<Share, StorageAccount, ShareClient>
    implements Deletable, IShareFile {

    private final ShareFileModule subFileModule;

    protected Share(@Nonnull String name, @Nonnull ShareModule module) {
        super(name, module);
        this.subFileModule = new ShareFileModule(this);
    }

    /**
     * copy constructor
     */
    public Share(@Nonnull Share origin) {
        super(origin);
        this.subFileModule = origin.subFileModule;
    }

    @Nonnull
    @Override
    public List<AbstractAzResourceModule<?, ?, ?>> getSubModules() {
        return Collections.singletonList(this.subFileModule);
    }

    @Nonnull
    @Override
    public String loadStatus(@Nonnull ShareClient remote) {
        return "";
    }

    @Override
    public ShareDirectoryClient getClient() {
        final ShareModule module = (ShareModule) this.getModule();
        final ShareServiceClient fileShareServiceClient = module.getFileShareServiceClient();
        final ShareClient shareClient = fileShareServiceClient.getShareClient(this.getName());
        return shareClient.getRootDirectoryClient();
    }

    public ShareClient getShareClient() {
        final ShareModule module = (ShareModule) this.getModule();
        final ShareServiceClient fileShareServiceClient = module.getFileShareServiceClient();
        return fileShareServiceClient.getShareClient(this.getName());
    }

    @Override
    public Share getShare() {
        return this;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public String getUrl() {
        final ShareModule module = (ShareModule) this.getModule();
        final ShareServiceClient fileShareServiceClient = module.getFileShareServiceClient();
        final ShareClient shareClient = fileShareServiceClient.getShareClient(this.getName());
        return shareClient.getShareUrl();
    }

    @Nullable
    @Override
    public OffsetDateTime getLastModified() {
        return this.remoteOptional().map(ShareClient::getProperties).map(ShareProperties::getLastModified).orElse(null);
    }

    @Override
    public void download(OutputStream output) {
    }

    @Override
    public void download(Path dest) {
    }
}
