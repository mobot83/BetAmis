{{/*
Standard labels for all infra resources.
*/}}
{{- define "betamis-infra.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Zookeeper connect string — derived from the release namespace.
*/}}
{{- define "betamis-infra.zookeeperConnect" -}}
zookeeper.{{ .Release.Namespace }}.svc.cluster.local:2181
{{- end }}

{{/*
Kafka bootstrap server — uses the configured advertisedHost.
*/}}
{{- define "betamis-infra.kafkaBootstrap" -}}
{{ .Values.kafka.advertisedHost }}:9092
{{- end }}
