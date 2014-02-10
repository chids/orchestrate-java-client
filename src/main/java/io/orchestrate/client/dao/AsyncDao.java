/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.orchestrate.client.dao;

import io.orchestrate.client.KvMetadata;
import io.orchestrate.client.KvObject;
import io.orchestrate.client.OrchestrateFutureListener;

import java.util.concurrent.Future;

/**
 * Interface for generic Create, Read, Update, and Delete (CRUD) operations on
 * a collection for the specified type.
 *
 * @param <T> The type this object will control CRUD operations for.
 */
public interface AsyncDao<T> {

    // TODO add support for annotation-based store operations

    /**
     * Return a {@code Future} for the object stored with the specified
     * {@code key}.
     *
     * @param key The key used to find the object.
     * @return A future for the object with this key, may return {@code null}.
     */
    Future<KvObject<T>> findOne(final String key);

    /**
     * Find the object stored with the specified {@code key} and fire the
     * {@code listeners} on the response.
     *
     * @param key The key used to find the object.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void findOne(final String key, final Iterable<OrchestrateFutureListener<KvObject<T>>> listeners);

    /**
     * Return a {@code Future} for the object stored with the specified
     * {@code key} and the version from the {@code metadata}.
     *
     * @param key The key used to find the object.
     * @param metadata The metadata about the version of the object to get.
     * @return A future for the object with this key and version (from the
     *         metadata), may return {@code null}.
     */
    Future<KvObject<T>> findOne(final String key, final KvMetadata metadata);

    /**
     * Find the object stored with the specified {@code key} and the version
     * from the {@code metadata} and fire the {@code listeners} on the response.
     *
     * @param key The key used to find the object.
     * @param metadata The metadata about the version of the object to get.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void findOne(final String key, final KvMetadata metadata, final Iterable<OrchestrateFutureListener<KvObject<T>>> listeners);

    /**
     * Return a {@code Future} for the object stored with the specified
     * {@code key} and the version {@code ref}.
     *
     * @param key The key used to find the object.
     * @param ref The version of the object.
     * @return A future for the object with this key and version, may return
     *         {@code null}.
     */
    Future<KvObject<T>> findOne(final String key, final String ref);

    /**
     * Find the object stored with the specified {@code key} and the version
     * {@code ref} and fire the {@code listeners} on the response.
     *
     * @param key The key used to find the object.
     * @param ref The version of the object.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void findOne(final String key, final String ref, final Iterable<OrchestrateFutureListener<KvObject<T>>> listeners);

    /**
     * Save the specified {@code value} to the {@code key}. This returns a
     * future with the {@link KvMetadata} about the saved object.
     *
     * @param key The key to save the specified {@code value} to.
     * @param value The object to save.
     * @return A future for the metadata about the saved object.
     */
    Future<KvMetadata> save(final String key, final T value);

    /**
     * Save the specified {@code value} to the {@code key} and fire the
     * {@code listeners} on the response.
     *
     * @param key The key to save the specified {@code value} to.
     * @param value The object to save.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void save(final String key, final T value, final Iterable<OrchestrateFutureListener<KvMetadata>> listeners);

    /**
     * Save the specified {@code value} to the {@code key} if and only if the
     * {@code key} does not already exist when {@code ifAbsent} is {@code true}.
     *
     * <p>If {@code ifAbsent} is {@code true} and the {@code key} already exists
     * the result of the {@code Future} will be {@code null}.
     *
     * @param key The key to save the specified {@code value} to.
     * @param value The object to save.
     * @param ifAbsent When {@code true} the store operation will only succeed
     *                 if the specified {@code key} does not already exist.
     * @return A future for the metadata about the saved object.
     */
    Future<KvMetadata> save(final String key, final T value, final boolean ifAbsent);

    /**
     * Save the specified {@code value} to the {@code key} if and only if the
     * {@code key} does not already exist when {@code ifAbsent} is {@code true}.
     * Fire the {@code listeners} on the response.
     *
     * <p>If {@code ifAbsent} is {@code true} and the {@code key} already exists
     * the result of the {@code Future} will be {@code null}.
     *
     * @param key The key to save the specified {@code value} to.
     * @param value The object to save.
     * @param ifAbsent When {@code true} the store operation will only succeed
     *                 if the specified {@code key} does not already exist.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void save(final String key, final T value, final boolean ifAbsent, final Iterable<OrchestrateFutureListener<KvMetadata>> listeners);

    /**
     * Save the specified {@code value} to the {@code key} if and only if the
     * ref from the {@code metadata} matches the current stored ref.
     *
     * <p>If the ref from the metadata does not match the current ref stored, the
     * result of the {@code Future} will be {@code null}.
     *
     * @param key The key to save the specified {@code value} to.
     * @param value The object to save.
     * @param metadata The metadata about the current object stored to this key.
     * @return A future for the metadata about the saved object.
     */
    Future<KvMetadata> save(final String key, final T value, final KvMetadata metadata);

    /**
     * Save the specified {@code value} to the {@code key} if and only if the
     * ref from the {@code metadata} matches the current stored ref. Fire the
     * {@code listeners} on the response.
     *
     * @param key The key to save the specified {@code value} to.
     * @param value The object to save.
     * @param metadata The metadata about the current object stored to this key.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void save(final String key, final T value, final KvMetadata metadata, final Iterable<OrchestrateFutureListener<KvMetadata>> listeners);

    /**
     * Save the specified {@code value} to the {@code key} if and only if the
     * {@code currentRef} matches the current stored ref.
     *
     * <p>If the {@code currentRef} does not match the current ref stored, the
     * result of the {@code Future} will be {@code null}.
     *
     * @param key The key to save the specified {@code value} to.
     * @param value The object to save.
     * @param currentRef The ref of the last known object stored to this key.
     * @return A future for the metadata about the saved object.
     */
    Future<KvMetadata> save(final String key, final T value, final String currentRef);

    /**
     * Save the specified {@code value} to the {@code key} if and only if the
     * {@code currentRef} matches the current stored ref. Fire the
     * {@code listeners} on the response.
     *
     * <p>If the {@code currentRef} does not match the current ref stored, the
     * result of the {@code Future} will be {@code null}.
     *
     * @param key The key to save the specified {@code value} to.
     * @param value The object to save.
     * @param currentRef The ref of the last known object stored to this key.
     * @param listeners Thhe callbacks to fire on the operation's response.
     */
    void save(final String key, final T value, final String currentRef, final Iterable<OrchestrateFutureListener<KvMetadata>> listeners);

    /**
     * Delete the object with the specified {@code key}.
     *
     * @param key The key of the object to delete.
     * @return A future with the success of the delete.
     */
    Future<Boolean> delete(final String key);

    /**
     * Delete the object with the specified {@code key} and fire the
     * {@code listeners} on the response.
     *
     * @param key The key of the object to delete.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void delete(final String key, final Iterable<OrchestrateFutureListener<Boolean>> listeners);

    /**
     * Delete the object with the specified {@code key} if and only if the ref
     * from the {@code metadata} matches the current stored ref.
     *
     * <p>If the ref from the metadata does not match the current ref stored, the
     * result of the {@code Future} will be {@code false}.
     *
     * @param key The key of the object to delete.
     * @param metadata The metadata about the current object stored to this key.
     * @return A future with the success of the delete.
     */
    Future<Boolean> delete(final String key, final KvMetadata metadata);

    /**
     * Delete the object with the specified {@code key} if and only if the ref
     * from the {@code metadata} matches the current stored ref.
     *
     * <p>If the ref from the metadata does not match the current ref stored, the
     * result of the {@code Future} will be {@code false}.
     *
     * @param key The key of the object to delete.
     * @param metadata The metadata about the current object stored to this key.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void delete(final String key, final KvMetadata metadata, final Iterable<OrchestrateFutureListener<Boolean>> listeners);

    /**
     * Delete the object with the specified {@code key} if and only if the
     * {@code currentRef} matches the current stored ref.
     *
     * <p>If the {@code currentRef} does not match the current ref stored, the
     * result of the {@code Future} will be {@code false}.
     *
     * @param key The key of the object to delete.
     * @param currentRef The ref of the last known object stored to this key.
     * @return A future with the success of the delete.
     */
    Future<Boolean> delete(final String key, final String currentRef);

    /**
     * Delete the object with the specified {@code key} if and only if the
     * {@code currentRef} matches the current stored ref.
     *
     * <p>If the {@code currentRef} does not match the current ref stored, the
     * result of the {@code Future} will be {@code false}.
     *
     * @param key The key of the object to delete.
     * @param currentRef The ref of the last known object stored to this key.
     * @param listeners The callbacks to fire on the operation's response.
     */
    void delete(final String key, final String currentRef, final Iterable<OrchestrateFutureListener<Boolean>> listeners);

    /**
     * Delete all objects in the collection.
     *
     * @return A future for the success of the delete.
     */
    Future<Boolean> deleteAll();

    /**
     * Delete all objects in the collection.
     *
     * @param listeners The callbacks to fire on the operation's response.
     */
    void deleteAll(final Iterable<OrchestrateFutureListener<Boolean>> listeners);

}
