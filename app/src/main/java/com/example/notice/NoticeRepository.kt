package com.example.notice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NoticeRepository {
    private val _notices = MutableStateFlow<List<NoticeItem>>(emptyList())
    val notices: StateFlow<List<NoticeItem>> = _notices.asStateFlow()

    fun addNotice(notice: NoticeItem) {
        _notices.value = listOf(notice) + _notices.value
    }

    fun deleteNotice(id: String) {
        _notices.value = _notices.value.filter { it.id != id }
    }

    fun clearAll() {
        _notices.value = emptyList()
    }
}
