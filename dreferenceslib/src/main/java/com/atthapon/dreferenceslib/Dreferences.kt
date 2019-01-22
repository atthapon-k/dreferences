package com.atthapon.dreferenceslib

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Build
import android.annotation.TargetApi
import android.text.TextUtils

/**
 * Created by Atthapon Korkaew on 22/1/2019 AD.
 */
class Dreferences {
    companion object {
        private const val DEFAULT_SUFFIX = "_preferences"
        private const val LENGTH = "#LENGTH"
        private var mPrefs: SharedPreferences? = null
        
        fun initPrefs(context: Context) {
            Builder().setContext(context).build()
        }

        private fun initPrefs(context: Context, prefsName: String, mode: Int) {
            mPrefs = context.getSharedPreferences(prefsName, mode)
        }

        fun getPreferences(): SharedPreferences {
            return mPrefs ?: throw  RuntimeException(
                "Prefs class not correctly instantiated. Please call Builder.setContext().build() in the Application class onCreate."
            )
        }

        fun getAll(): Map<String, *> {
            return getPreferences().all
        }

        fun getInt(key: String, defValue: Int): Int {
            return getPreferences().getInt(key, defValue)
        }

        fun getInt(key: String): Int {
            return getPreferences().getInt(key, 0)
        }

        fun getBoolean(key: String, defValue: Boolean): Boolean {
            return getPreferences().getBoolean(key, defValue)
        }

        fun getBoolean(key: String): Boolean {
            return getPreferences().getBoolean(key, false)
        }

        fun getLong(key: String, defValue: Long): Long {
            return getPreferences().getLong(key, defValue)
        }

        fun getLong(key: String): Long {
            return getPreferences().getLong(key, 0L)
        }

        fun getDouble(key: String, defValue: Double): Double {
            return java.lang.Double.longBitsToDouble(
                getPreferences().getLong(
                    key,
                    java.lang.Double.doubleToLongBits(defValue)
                )
            )
        }

        fun getDouble(key: String): Double {
            return java.lang.Double.longBitsToDouble(
                getPreferences().getLong(
                    key,
                    java.lang.Double.doubleToLongBits(0.0)
                )
            )
        }

        fun getFloat(key: String, defValue: Float): Float {
            return getPreferences().getFloat(key, defValue)
        }

        fun getFloat(key: String): Float {
            return getPreferences().getFloat(key, 0.0f)
        }

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun getString(key: String, defValue: String): String {
            return getPreferences().getString(key, defValue)
        }

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun getString(key: String): String {
            return getPreferences().getString(key, "")
        }

        @SuppressLint("ObsoleteSdkInt")
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        fun getStringSet(key: String, defValue: Set<String>): Set<String?>? {
            val prefs = getPreferences()
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                prefs.getStringSet(key, defValue)
            } else {
                getOrderedStringSet(key, defValue)
            }
        }

        fun getOrderedStringSet(key: String, defValue: Set<String>): Set<String?> {
            val prefs = getPreferences()
            if (prefs.contains(key + LENGTH)) {
                val set = LinkedHashSet<String?>()
                val stringSetLength = prefs.getInt(key + LENGTH, -1)
                if (stringSetLength >= 0) {
                    for (i in 0 until stringSetLength) {
                        set.add(prefs.getString("$key[$i]", null))
                    }
                }
                return set
            }
            return defValue
        }

        fun putLong(key: String, value: Long) {
            val editor = getPreferences().edit()
            editor.putLong(key, value)
            editor.apply()
        }

        fun putInt(key: String, value: Int) {
            val editor = getPreferences().edit()
            editor.putInt(key, value)
            editor.apply()
        }

        fun putDouble(key: String, value: Double) {
            val editor = getPreferences().edit()
            editor.putLong(key, java.lang.Double.doubleToRawLongBits(value))
            editor.apply()
        }

        fun putFloat(key: String, value: Float) {
            val editor = getPreferences().edit()
            editor.putFloat(key, value)
            editor.apply()
        }

        fun putBoolean(key: String, value: Boolean) {
            val editor = getPreferences().edit()
            editor.putBoolean(key, value)
            editor.apply()
        }

        fun putString(key: String, value: String) {
            val editor = getPreferences().edit()
            editor.putString(key, value)
            editor.apply()
        }

        @SuppressLint("ObsoleteSdkInt")
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        fun putStringSet(key: String, value: Set<String>) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                val editor = getPreferences().edit()
                editor.putStringSet(key, value)
                editor.apply()
            } else {
                // Workaround for pre-HC's lack of StringSets
                putOrderedStringSet(key, value)
            }
        }

        fun putOrderedStringSet(key: String, value: Set<String>) {
            val editor = getPreferences().edit()
            var stringSetLength = 0
            if (mPrefs?.contains(key + LENGTH) == true) {
                // First read what the value was
                stringSetLength = mPrefs?.getInt(key + LENGTH, -1) ?: stringSetLength
            }
            editor.putInt(key + LENGTH, value.size)
            var i = 0
            for (aValue in value) {
                editor.putString("$key[$i]", aValue)
                i++
            }
            while (i < stringSetLength) {
                // Remove any remaining values
                editor.remove("$key[$i]")
                i++
            }
            editor.apply()
        }

        fun remove(key: String) {
            val prefs = getPreferences()
            val editor = prefs.edit()
            if (prefs.contains(key + LENGTH)) {
                // Workaround for pre-HC's lack of StringSets
                val stringSetLength = prefs.getInt(key + LENGTH, -1)
                if (stringSetLength >= 0) {
                    editor.remove(key + LENGTH)
                    for (i in 0 until stringSetLength) {
                        editor.remove("$key[$i]")
                    }
                }
            }
            editor.remove(key)

            editor.apply()
        }

        fun contains(key: String): Boolean {
            return getPreferences().contains(key)
        }

        fun clear(): SharedPreferences.Editor {
            val editor = getPreferences().edit().clear()
            editor.apply()
            return editor
        }

        fun edit(): SharedPreferences.Editor {
            return getPreferences().edit()
        }

    }

    internal class Builder {
        private lateinit var mKey: String
        private lateinit var mContext: Context
        private var mMode = -1
        private var mUseDefault = false

        fun setPrefsName(prefsName: String): Builder {
            mKey = prefsName
            return this
        }

        fun setContext(context: Context): Builder {
            mContext = context
            return this
        }

        /**
         * @deprecated
         * MODE_WORLD_READABLE, MODE_WORLD_WRITEABLE,MODE_MULTI_PROCESS
         * These mode were deprecated in API level 17.
         */
        @SuppressLint("WorldReadableFiles", "WorldWriteableFiles")
        fun setMode(mode: Int): Builder {
            if (mode == ContextWrapper.MODE_PRIVATE
                || mode == ContextWrapper.MODE_WORLD_READABLE
                || mode == ContextWrapper.MODE_WORLD_WRITEABLE
                || mode == ContextWrapper.MODE_MULTI_PROCESS
            ) {
                mMode = mode
            } else {
                throw  RuntimeException(
                    "The mode in the SharedPreference can only be set too ContextWrapper.MODE_PRIVATE," +
                            " ContextWrapper.MODE_WORLD_READABLE, ContextWrapper.MODE_WORLD_WRITEABLE or ContextWrapper.MODE_MULTI_PROCESS"
                )
            }
            return this
        }

        fun setUseDefaultSharedPreference(defaultSharedPreference: Boolean): Builder {
            mUseDefault = defaultSharedPreference
            return this
        }

        fun build() {
            if (TextUtils.isEmpty(mKey)) mKey = mContext.packageName
            if (mUseDefault) mKey += DEFAULT_SUFFIX
            if (mMode == -1) mMode = ContextWrapper.MODE_PRIVATE
            Dreferences.initPrefs(mContext, mKey, mMode)
        }
    }
}