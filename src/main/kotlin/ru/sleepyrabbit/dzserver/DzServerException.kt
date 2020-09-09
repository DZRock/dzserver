package ru.sleepyrabbit.dzserver

class InvalidProcessorConfigurationException(className: String): Exception(className)

class IncorrectRouteException(key: String): Exception(key)