/*
 *  Copyright (C) 2013  Tobias Preuss, Peter Vasil
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.avpptr.umweltzone.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.util.LruCache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import de.avpptr.umweltzone.R;
import de.avpptr.umweltzone.Umweltzone;
import de.avpptr.umweltzone.analytics.TrackingPoint;
import de.avpptr.umweltzone.models.Faq;
import de.avpptr.umweltzone.models.LowEmissionZone;

public abstract class ContentProvider {

    private static LruCache<String, Integer> filePathResourceIdCache = null;

    public static List<Faq> getFaqs(final Context context) {
        return getContent(context, "faqs_de", Faq.class);
    }

    public static List<LowEmissionZone> getLowEmissionZones(final Context context) {
        return getContent(context, "zones_de", LowEmissionZone.class);
    }

    public static List<GeoPoint> getCircuitPoints(final Context context, final String fileName) {
        return getContent(context, fileName, GeoPoint.class);
    }

    private static <T> List<T> getContent(final Context context, final String fileName, Class<T> contentType) {
        return getContent(context, fileName, "raw", contentType);
    }

    private static <T> List<T> getContent(final Context context, final String fileName, final String folderName, Class<T> contentType) {
        // Invoke cache
        int rawResourceId = getResourceIdForFileName(context, fileName, folderName);
        if (rawResourceId == de.avpptr.umweltzone.contract.Resources.INVALID_RESOURCE_ID) {
            final String filePath = folderName + "/" + fileName;
            Umweltzone.getTracker().trackError(TrackingPoint.ResourceNotFoundError, filePath);
            throw new IllegalStateException("Resource for file path '" + filePath + "' not found.");
        }
        InputStream inputStream = context.getResources().openRawResource(rawResourceId);
        ObjectMapper objectMapper = new ObjectMapper();
        String datePattern = context.getString(R.string.config_zone_number_since_date_format);
        objectMapper.setDateFormat(new SimpleDateFormat(datePattern));
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            CollectionType collectionType = typeFactory.constructCollectionType(List.class, contentType);
            return objectMapper.readValue(inputStream, collectionType);
        } catch (IOException e) {
            // TODO Aware that app will crash when JSON is mis-structured.
            e.printStackTrace();
        }
        return null;
    }

    // Returns a valid resource id or 0 if the resource cannot be found
    private static int getResourceIdForFileName(final Context context, final String fileName, final String folderName) {
        final String filePath = getFilePath(fileName, folderName);
        if (filePathResourceIdCache == null) {
            // Initialize cache
            filePathResourceIdCache = new LruCache<String, Integer>(6);
            return getAndCacheResourceIdForFileName(context, fileName, folderName);
        } else {
            // Try loading from cache
            Integer rawResourceId = filePathResourceIdCache.get(filePath);
            if (rawResourceId == null) {
                rawResourceId = getAndCacheResourceIdForFileName(context, fileName, folderName);
            }
            return rawResourceId;
        }
    }

    private static int getAndCacheResourceIdForFileName(final Context context, final String fileName, final String folderName) {
        final Resources resources = context.getResources();
        final String filePath = getFilePath(fileName, folderName);
        // Look-up identifier using reflection (expensive)
        int rawResourceId = resources.getIdentifier(fileName, folderName, context.getPackageName());
        // Store in cache if not 0
        if (rawResourceId != de.avpptr.umweltzone.contract.Resources.INVALID_RESOURCE_ID) {
            filePathResourceIdCache.put(filePath, rawResourceId);
        }
        return rawResourceId;
    }

    private static String getFilePath(final String fileName, final String folderName) {
        return folderName + "/" + fileName;
    }

}
