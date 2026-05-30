CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT,
    email_hash VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TYPE content_type AS ENUM ('TEXT', 'IMAGE', 'PDF', 'HIGHLIGHT', 'NOTE');

CREATE TABLE study_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    heading VARCHAR(255),
    started_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    parent_topic_id UUID REFERENCES topics(id),
    embedding_ref VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE topic_clusters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    user_id UUID REFERENCES users(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE topic_cluster_topics (
    topic_cluster_id UUID REFERENCES topic_clusters(id),
    topic_id UUID REFERENCES topics(id),
    PRIMARY KEY (topic_cluster_id, topic_id)
);

CREATE TABLE content_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    type content_type NOT NULL,
    raw_content TEXT,
    source_url TEXT,
    page_title VARCHAR(255),
    mime_type VARCHAR(100),
    minio_key VARCHAR(255),
    checksum VARCHAR(255) NOT NULL,
    session_id UUID REFERENCES study_sessions(id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE content_item_topics (
    content_item_id UUID REFERENCES content_items(id),
    topic_id UUID REFERENCES topics(id),
    PRIMARY KEY (content_item_id, topic_id)
);

CREATE TABLE highlights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_item_id UUID NOT NULL REFERENCES content_items(id),
    user_id UUID NOT NULL REFERENCES users(id),
    selected_text TEXT,
    surrounding_context TEXT,
    page_url TEXT,
    page_title VARCHAR(255),
    highlight_color VARCHAR(50) DEFAULT 'yellow',
    position_data JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resurfacing_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    content_item_id UUID NOT NULL REFERENCES content_items(id),
    triggered_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    context_url TEXT
);

CREATE TABLE exam_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255),
    exam_date DATE,
    syllabus_text TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_topic_coverages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_profile_id UUID REFERENCES exam_profiles(id),
    topic_id UUID REFERENCES topics(id),
    coverage_percent NUMERIC(5,2),
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE compiled_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255),
    compiled_text TEXT,
    minio_key VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);